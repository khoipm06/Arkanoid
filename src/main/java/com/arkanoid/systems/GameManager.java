package com.arkanoid.systems;

import com.arkanoid.core.GameConstants;
import com.arkanoid.core.entities.*;
import com.arkanoid.core.physics.CollisionDetector;
import com.arkanoid.systems.level.LevelManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.Player;
import com.arkanoid.systems.save.GameStateMapper;
import com.arkanoid.ui.view.SessionManager;
import com.arkanoid.utils.ObjectPool;
import javafx.scene.paint.Color;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Coordinates single-player gameplay simulation, level progression, and session lifecycle.
 */
public class GameManager {
    private static final Logger logger = GameLogger.getLogger(GameManager.class);
    private static final Random random = new Random();

    private GameState currentState;
    private final LevelManager levelManager;
    private final PlayerManager playerManager;
    private final VisualEffectsManager effectsManager;
    private final List<Ball> balls;
    private List<Brick> bricks;
    private final List<PowerUp> powerUps;
    private final List<Bullet> bullets;
    private final ObjectPool<Bullet> bulletPool;
    private final Player player;
    private final double gameWidth;
    private final double gameHeight;
    private final int levelNumber;

    private double gunFireCooldown = 0.0;
    private double elapsedTimeSeconds = 0.0;

    private GameManager(double gameWidth, double gameHeight, int levelNumber) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.levelNumber = levelNumber;
        this.currentState = GameState.MENU;
        this.levelManager = new LevelManager();
        this.playerManager = PlayerManager.getInstance();
        this.effectsManager = new VisualEffectsManager();
        this.balls = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.bullets = new ArrayList<>();

        this.bulletPool = new ObjectPool<>(
                () -> new Bullet(0, 0, GameConstants.DEFAULT_BULLET_WIDTH,
                        GameConstants.DEFAULT_BULLET_HEIGHT, GameConstants.DEFAULT_BULLET_SPEED),
                bullet -> bullet.setActive(false),
                GameConstants.MAX_BULLETS
        );
        bulletPool.prewarm(10);

        this.bricks = levelManager.loadLevel(levelNumber);
        Paddle paddle = new Paddle(
                gameWidth / 2 - 30,
                gameHeight - 30,
                GameConstants.DEFAULT_PADDLE_WIDTH,
                GameConstants.DEFAULT_PADDLE_HEIGHT,
                GameConstants.DEFAULT_PADDLE_SPEED,
                0,
                gameWidth
        );

        SessionManager.User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            String equippedSkin = currentUser.getEquippedPaddleSkin();
            paddle.equipSkin(equippedSkin);
            logger.debug("Applied equipped paddle skin: {}", equippedSkin);
        }

        this.player = new Player("Player1", 1, paddle);
        playerManager.addPlayer(1, player);
    }

    public static GameManager getInstance(double gameWidth, double gameHeight, int levelNumber) {
        return new GameManager(gameWidth, gameHeight, levelNumber);
    }

    public void startGame() {
        currentState = GameState.PLAYING;
        elapsedTimeSeconds = 0.0;

        Paddle paddle = player.getPaddle();
        double ballRadius = GameConstants.DEFAULT_BALL_RADIUS;
        double ballX = paddle.getX() + paddle.getWidth() / 2;
        double ballY = paddle.getY() - ballRadius * 2;
        Ball ball = new Ball(ballX, ballY, ballRadius, GameConstants.DEFAULT_BALL_SPEED);
        ball.setBounds(0, 0, gameWidth, gameHeight);

        SessionManager.User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            String equippedBallSkin = currentUser.getEquippedBallSkin();
            ball.equipSkin(equippedBallSkin);
            logger.debug("Applied equipped ball skin: {}", equippedBallSkin);
        }

        balls.add(ball);
    }

    public void loadLevel(int levelNumber) {
        bulletPool.releaseAll(bullets);
        bullets.clear();
        powerUps.clear();
        effectsManager.clearAndReleaseAll();
        bricks = levelManager.loadLevel(levelNumber);
    }

    public void update(double deltaTime) {
        if (currentState != GameState.PLAYING || !Double.isFinite(deltaTime) || deltaTime <= 0.0) {
            return;
        }

        double clampedDelta = Math.min(deltaTime, GameConstants.MAX_FRAME_DELTA);
        elapsedTimeSeconds += clampedDelta;

        double remaining = clampedDelta;
        while (remaining > 0.0 && currentState == GameState.PLAYING) {
            double step = Math.min(remaining, GameConstants.MAX_PHYSICS_STEP);
            stepSimulation(step);
            remaining -= step;
        }

        effectsManager.spawnBallTrails(balls, clampedDelta);
        effectsManager.update(clampedDelta);
    }

    private void stepSimulation(double step) {
        player.update(step);
        Paddle paddle = player.getPaddle();

        for (Ball ball : balls) {
            if (ball.isAttachedToPaddle()) {
                ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getRadius());
                ball.setY(paddle.getY() - ball.getRadius() * 2);
            }
            ball.update(step);
            ball.checkPaddleCollision(paddle);
        }

        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                brick.update(step);
            }
        }

        updatePowerUps(step, paddle);

        for (Ball ball : balls) {
            CollisionDetector.checkBallBrickCollisions(ball, bricks, this::onBrickDestroyed, this);
        }
        bricks.removeIf(Brick::isDestroyed);

        Iterator<Ball> ballIt = balls.iterator();
        while (ballIt.hasNext()) {
            Ball ball = ballIt.next();
            if (ball.isOutOfBounds()) {
                player.getState().loseLife();
                if (player.getState().isGameOver()) {
                    currentState = GameState.GAME_OVER;
                }
                ballIt.remove();
            }
        }

        if (balls.isEmpty() && currentState == GameState.PLAYING) {
            resetBall(paddle);
        }

        updateBullets(step, paddle);

        bricks.removeIf(Brick::isDestroyed);
        if (currentState == GameState.PLAYING && areAllBreakableBricksDestroyed()) {
            currentState = GameState.LEVEL_COMPLETE;
        }
    }

    private void updatePowerUps(double step, Paddle paddle) {
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.update(step);

            if (powerUp.checkPaddleCollision(paddle)) {
                player.getState().addScore(50);
                effectsManager.spawnFloatingText("+50", powerUp.getCenterX(), powerUp.getCenterY(), 1.0, 50, Color.YELLOW);
                applyPowerUpEffect(powerUp);
                powerUpIterator.remove();
                continue;
            }

            if (!powerUp.isActive() || powerUp.getY() > gameHeight) {
                powerUpIterator.remove();
            }
        }
    }

    private void updateBullets(double step, Paddle paddle) {
        if (paddle.isGunMode()) {
            gunFireCooldown -= step;
            if (gunFireCooldown <= 0.0) {
                Bullet left = bulletPool.acquire();
                left.reset(paddle.getLeftGunX(), paddle.getGunY(),
                        GameConstants.DEFAULT_BULLET_WIDTH, GameConstants.DEFAULT_BULLET_HEIGHT,
                        GameConstants.DEFAULT_BULLET_SPEED);
                Bullet right = bulletPool.acquire();
                right.reset(paddle.getRightGunX(), paddle.getGunY(),
                        GameConstants.DEFAULT_BULLET_WIDTH, GameConstants.DEFAULT_BULLET_HEIGHT,
                        GameConstants.DEFAULT_BULLET_SPEED);
                bullets.add(left);
                bullets.add(right);
                gunFireCooldown = GameConstants.GUN_FIRE_INTERVAL;
            }
        } else {
            gunFireCooldown = 0.0;
        }

        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update(step);

            if (bullet.isOutOfBounds(gameHeight)) {
                bulletIt.remove();
                bulletPool.release(bullet);
                continue;
            }

            boolean hit = false;
            for (Brick brick : bricks) {
                if (brick.isDestroyed()) {
                    continue;
                }
                if (bullet.intersects((GameObject) brick)) {
                    brick.hit();
                    if (brick.isDestroyed()) {
                        onBrickDestroyed(brick);
                    }
                    hit = true;
                    break;
                }
            }

            if (hit) {
                bulletIt.remove();
                bulletPool.release(bullet);
            }
        }
    }

    private boolean areAllBreakableBricksDestroyed() {
        for (Brick brick : bricks) {
            if (!(brick instanceof UnbreakableBrick) && !brick.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    public void addExplosion(double x, double y, double frameWidth, double frameHeight, double duration) {
        effectsManager.spawnExplosion(x, y, frameWidth, frameHeight, duration);
    }

    public void onBrickDestroyed(Brick brick) {
        if (brick.isDestroyed()) {
            player.getState().addScore(100);
            effectsManager.spawnFloatingText("+100", brick.getCenterX(), brick.getCenterY(), 1.0, 50, Color.CORAL);
            PowerUp powerUp = brick.dropPowerUp();
            if (powerUp != null) {
                powerUps.add(powerUp);
            }
            effectsManager.spawnBrickParticles(brick.getCenterX(), brick.getCenterY(), brick.getColor(), 15);
        }
    }

    private void resetBall(Paddle paddle) {
        double ballRadius = GameConstants.DEFAULT_BALL_RADIUS;
        double ballX = paddle.getX() + paddle.getWidth() / 2;
        double ballY = paddle.getY() - ballRadius * 2;
        Ball ball = new Ball(ballX, ballY, ballRadius, GameConstants.DEFAULT_BALL_SPEED);
        ball.setBounds(0, 0, gameWidth, gameHeight);
        ball.setAttachedToPaddle(true);
        ball.setExplosive(false);
        ball.setHasExploded(false);
        balls.add(ball);
    }

    private void applyPowerUpEffect(PowerUp powerUp) {
        logger.debug("Applying power-up effect: {}", powerUp.getClass().getSimpleName());

        if (powerUp instanceof ExplosiveBallPowerUp) {
            for (Ball ball : balls) {
                ball.setExplosive(true);
                ball.setHasExploded(false);
            }
        } else if (powerUp instanceof RowClearPowerUp) {
            boolean clearRow = random.nextBoolean();
            int maxRow = bricks.stream().mapToInt(Brick::getRow).max().orElse(0);
            int maxCol = bricks.stream().mapToInt(Brick::getCol).max().orElse(0);

            if (clearRow) {
                int rowToClear = random.nextInt(maxRow + 1);
                double y = bricks.stream()
                        .filter(b -> b.getRow() == rowToClear)
                        .findFirst()
                        .map(Brick::getY)
                        .orElse(0.0);
                effectsManager.spawnLineEffect(0, y, gameWidth, y, 0.5);
                clearBricksInLine(true, rowToClear);
            } else {
                int colToClear = random.nextInt(maxCol + 1);
                double x = bricks.stream()
                        .filter(b -> b.getCol() == colToClear)
                        .findFirst()
                        .map(Brick::getX)
                        .orElse(0.0);
                effectsManager.spawnLineEffect(x, 0, x, gameHeight, 0.5);
                clearBricksInLine(false, colToClear);
            }
        }
    }

    private void clearBricksInLine(boolean byRow, int targetIndex) {
        for (Brick brick : bricks) {
            if (brick.isDestroyed() || brick instanceof UnbreakableBrick) {
                continue;
            }
            int brickIndex = byRow ? brick.getRow() : brick.getCol();
            if (brickIndex == targetIndex) {
                brick.destroy();
                onBrickDestroyed(brick);
            }
        }
    }

    public com.arkanoid.systems.save.GameState extractCurrentGameState() {
        return GameStateMapper.toGameState(levelNumber, elapsedTimeSeconds, player, balls, bricks, powerUps);
    }

    public void restoreGameState(com.arkanoid.systems.save.GameState gameState) {
        effectsManager.clearAndReleaseAll();
        bulletPool.releaseAll(bullets);
        bullets.clear();
        this.elapsedTimeSeconds = GameStateMapper.applyGameState(
                gameState, gameWidth, gameHeight, player, balls, bricks, powerUps);
    }

    /**
     * Releases pooled entities, clears active lists, and detaches session references.
     */
    public void cleanup() {
        bulletPool.releaseAll(bullets);
        bullets.clear();
        bulletPool.clear();
        powerUps.clear();
        balls.clear();
        bricks.clear();
        effectsManager.cleanup();
        playerManager.removePlayer(player.getPlayerNumber(), player);
    }

    public void pause() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
        }
    }

    public void resume() {
        if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
        }
    }

    public void togglePause() {
        if (currentState == GameState.PLAYING) {
            pause();
        } else if (currentState == GameState.PAUSED) {
            resume();
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState state) {
        this.currentState = state;
    }

    public VisualEffectsManager getVisualEffectsManager() {
        return effectsManager;
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getScore() {
        return player.getState().getScore();
    }

    public List<Explosion> getExplosions() {
        return effectsManager.getExplosions();
    }

    public List<LineEffect> getLineEffects() {
        return effectsManager.getLineEffects();
    }

    public List<Particle> getParticles() {
        return effectsManager.getParticles();
    }

    public List<TrailEffect> getTrailEffects() {
        return effectsManager.getTrailEffects();
    }

    public List<FloatingText> getFloatingTexts() {
        return effectsManager.getFloatingTexts();
    }

    public double getElapsedTimeSeconds() {
        return elapsedTimeSeconds;
    }

    public void setElapsedTimeSeconds(double elapsedTimeSeconds) {
        this.elapsedTimeSeconds = elapsedTimeSeconds;
    }

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }
}
