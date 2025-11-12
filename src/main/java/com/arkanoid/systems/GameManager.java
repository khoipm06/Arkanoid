package com.arkanoid.systems;

import com.arkanoid.core.entities.*;
import com.arkanoid.core.physics.CollisionDetector;
import com.arkanoid.systems.level.LevelManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.Player;
import com.arkanoid.systems.save.*;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static final double GUN_FIRE_INTERVAL = 0.2; // 0.2s ~ 5 bullets/sec
    private GameState currentState;
    private final LevelManager levelManager;
    private final PlayerManager playerManager;
    private final List<Ball> balls;
    private List<Brick> bricks;
    private final List<PowerUp> powerUps;
    private final Player player;
    private final double gameWidth;
    private final double gameHeight;
    private final int levelNumber;
    private final List<Explosion> explosions;
    private final List<LineEffect> lineEffects;
    private final List<Particle> particles;
    private final List<TrailEffect> trailEffects; // New field for trail effects
    private double trailSpawnTimer = 0.0; // Timer for spawning trail effects
    private static final double TRAIL_SPAWN_INTERVAL = 0.02; // Spawn a trail effect every 0.02 seconds

    private final List<Bullet> bullets;
    private double gunFireCooldown = 0.0;
    private final List<FloatingText> floatingTexts;

    public GameManager(double gameWidth, double gameHeight, int levelNumber) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.currentState = GameManager.GameState.MENU;
        this.levelManager = new LevelManager();
        this.playerManager = PlayerManager.getInstance();
        this.balls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.lineEffects = new ArrayList<>();
        this.particles = new ArrayList<>();
        this.trailEffects = new ArrayList<>();
        this.floatingTexts = new ArrayList<>();
        this.levelNumber = levelNumber;
        this.bricks = levelManager.loadLevel(levelNumber);
        Paddle paddle = new Paddle(gameWidth / 2 - 30, gameHeight - 30, 100, 25, 400, 0, gameWidth);
        player = new Player("Player1", 1, paddle);
        playerManager.addPlayer(1, player);
    }

    public void startGame() {
        currentState = GameManager.GameState.PLAYING;

        double ballRadius = 8;
        double ballSpeed = 300;

        Paddle paddle = player.getPaddle();
        double ballX = paddle.getX() + paddle.getWidth() / 2;
        double ballY = paddle.getY() - ballRadius * 2;
        Ball ball = new Ball(ballX, ballY, ballRadius, ballSpeed);
        ball.setBounds(0, 0, gameWidth, gameHeight);
        balls.add(ball);
    }

    public void loadLevel(int levelNumber) {
        bricks = levelManager.loadLevel(levelNumber);
        powerUps.clear();
        bullets.clear();
    }

    public void update(double deltaTime) {
        if (currentState != GameManager.GameState.PLAYING)
            return;

        try {
            playerManager.update(deltaTime);
            Paddle paddle = player.getPaddle();

            trailSpawnTimer += deltaTime;
        boolean canSpawnTrail = trailSpawnTimer >= TRAIL_SPAWN_INTERVAL;

        for (Ball ball : balls) {
            if (ball.isAttachedToPaddle()) {
                ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getRadius());
                ball.setY(paddle.getY() - ball.getRadius() * 2);
            } else {
                if (canSpawnTrail) {
                    trailEffects.add(new TrailEffect(ball.getCenterX(), ball.getCenterY(), ball.getRadius(), 0.5,
                            Color.CYAN));
                }
            }
            ball.update(deltaTime);
            ball.checkPaddleCollision(paddle);
        }

        if (canSpawnTrail) {
            trailSpawnTimer = 0.0;
        }

        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                brick.update(deltaTime);
            }
        }

        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.update(deltaTime);

            if (powerUp.checkPaddleCollision(player.getPaddle())) {
                player.getState().addScore(50);
                floatingTexts.add(new FloatingText("+50", powerUp.getCenterX(), powerUp.getCenterY(), 1.0, 50,
                        Color.YELLOW)); // Floating text for power-up
                applyPowerUpEffect(powerUp, paddle); // 🔥 kích hoạt hiệu ứng power-up
                powerUpIterator.remove(); // ❌ xoá luôn khỏi list sau khi ăn
                continue;
            }

            if (!powerUp.isActive() || powerUp.getY() > gameHeight) {
                powerUpIterator.remove();
            }
        }

        for (Ball ball : balls) {
            CollisionDetector.checkBallBrickCollisions(ball, bricks, this::onBrickDestroyed, this);
        }
        bricks.removeIf(Brick::isDestroyed);

        balls.removeIf(ball -> {
            if (ball.isOutOfBounds()) {
                player.getState().loseLife();
                if (player.getState().isGameOver()) {
                    currentState = GameManager.GameState.GAME_OVER;
                }
                return true;
            }
            return false;
        });

        if (balls.isEmpty() && currentState == GameManager.GameState.PLAYING) {
            resetBall(paddle);
        }

        if (bricks.isEmpty()) {
            currentState = GameManager.GameState.LEVEL_COMPLETE;
        }
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update(deltaTime);
            if (!explosion.isActive()) {
                explosionIterator.remove();
            }
        }

        Iterator<LineEffect> lineEffectIterator = lineEffects.iterator();
        while (lineEffectIterator.hasNext()) {
            LineEffect lineEffect = lineEffectIterator.next();
            lineEffect.update(deltaTime);
            if (!lineEffect.isActive()) {
                lineEffectIterator.remove();
            }
        }
        if (paddle.isGunMode()) {
            gunFireCooldown -= deltaTime;
            if (gunFireCooldown <= 0.0) {
                double bulletW = 6;
                double bulletH = 12;
                double speed = -300;
                Bullet left = new Bullet(paddle.getLeftGunX(), paddle.getGunY(), bulletW, bulletH, speed);
                Bullet right = new Bullet(paddle.getRightGunX(), paddle.getGunY(), bulletW, bulletH, speed);
                bullets.add(left);
                bullets.add(right);
                gunFireCooldown = GUN_FIRE_INTERVAL;
            }
        } else {
            // reset cooldown to allow immediate fire when re-activated
            gunFireCooldown = 0.0;
        }

        // update bullets
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet b = bulletIt.next();
            b.update(deltaTime);

            // check out of bounds (ở trên màn hình)
            if (b.isOutOfBounds(gameHeight)) {
                bulletIt.remove();
                continue;
            }

            // check collision with bricks
            boolean hit = false;
            Iterator<Brick> brickIt = bricks.iterator();
            while (brickIt.hasNext()) {
                Brick brick = brickIt.next();
                if (brick.isDestroyed())
                    continue;

                if (b.intersects((GameObject) brick)) {
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
            }
        }

        // Check win condition after all brick updates
        if (bricks.isEmpty()) {
            currentState = GameManager.GameState.LEVEL_COMPLETE;
        }

        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle particle = particleIterator.next();
            particle.update(deltaTime);
            if (!particle.isActive()) {
                particleIterator.remove();
            }
        }

        Iterator<TrailEffect> trailIterator = trailEffects.iterator();
        while (trailIterator.hasNext()) {
            TrailEffect trail = trailIterator.next();
            trail.update(deltaTime);
            if (!trail.isActive()) {
                trailIterator.remove();
            }
        }

        Iterator<FloatingText> textIterator = floatingTexts.iterator();
        while (textIterator.hasNext()) {
            FloatingText text = textIterator.next();
            text.update(deltaTime);
            if (!text.isActive()) {
                textIterator.remove();
            }
        }
        
        } catch (ConcurrentModificationException e) {
            GameLogger.error("ConcurrentModificationException in GameManager.update(): {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            GameLogger.error("Exception in GameManager.update(): {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<LineEffect> getLineEffects() {
        return lineEffects;
    }

    public List<TrailEffect> getTrailEffects() {
        return trailEffects;
    }

    public List<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }

    public void addExplosion(double x, double y, double frameWidth, double frameHeight, double duration) {
        explosions.add(new Explosion(x, y, 64, 64, 1));
    }

    public void onBrickDestroyed(Brick brick) {
        if (brick.isDestroyed()) {
            player.getState().addScore(100);
            floatingTexts.add(new FloatingText("+100", brick.getCenterX(), brick.getCenterY(), 1.0, 50,
                    Color.CORAL));
            PowerUp powerUp = brick.dropPowerUp();
            if (powerUp != null) {
                powerUps.add(powerUp);
            }

            int particleCount = 15;
            for (int i = 0; i < particleCount; i++) {
                particles.add(new Particle(brick.getCenterX(), brick.getCenterY(), brick.getColor()));
            }
        }
    }

    private void resetBall(Paddle paddle) {
        double ballRadius = 8;
        double ballSpeed = 300;

        double ballX = paddle.getX() + paddle.getWidth() / 2;
        double ballY = paddle.getY() - ballRadius * 2;
        Ball ball = new Ball(ballX, ballY, ballRadius, ballSpeed);
        ball.setBounds(0, 0, gameWidth, gameHeight);
        ball.setAttachedToPaddle(true);
        ball.setExplosive(false);
        ball.setHasExploded(false);
        balls.add(ball);
    }

    public void pause() {
        if (currentState == GameManager.GameState.PLAYING) {
            currentState = GameManager.GameState.PAUSED;
        }
    }

    public void resume() {
        if (currentState == GameManager.GameState.PAUSED) {
            currentState = GameManager.GameState.PLAYING;
        }
    }

    public void togglePause() {
        if (currentState == GameManager.GameState.PLAYING) {
            pause();
        } else if (currentState == GameManager.GameState.PAUSED) {
            resume();
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState state) {
        this.currentState = state;
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
        return explosions;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    private void applyPowerUpEffect(PowerUp powerUp, Paddle paddle) {
        GameLogger.debug("Applying power-up effect: {}", powerUp.getClass().getSimpleName());
        GameLogger.logCollectionState("bricks", bricks);
        
        try {
            if (powerUp instanceof ExplosiveBallPowerUp) {
                GameLogger.debug("Activating explosive ball for {} balls", balls.size());
                for (Ball ball : balls) {
                    ball.setExplosive(true);
                    ball.setHasExploded(false);
                }
            }
            // Other power-up effects will be added here
            else if (powerUp instanceof RowClearPowerUp) {
                Random random = new Random();
                boolean clearRow = random.nextBoolean(); // true for row, false for column

                int maxRow = bricks.stream().mapToInt(Brick::getRow).max().orElse(0);
                int maxCol = bricks.stream().mapToInt(Brick::getCol).max().orElse(0);

                if (clearRow) {
                    int rowToClear = random.nextInt(maxRow + 1);
                    GameLogger.debug("Clearing row {} (max row: {})", rowToClear, maxRow);
                    
                    double y = bricks.stream().filter(b -> b.getRow() == rowToClear).findFirst().map(Brick::getY)
                            .orElse(0.0);
                    lineEffects.add(new LineEffect(0, y, gameWidth, y, 0.5));
                    
                    // FIX: Use iterator instead of for-each to safely modify collection during iteration
                    Iterator<Brick> brickIterator = bricks.iterator();
                    int clearedCount = 0;
                    while (brickIterator.hasNext()) {
                        Brick brick = brickIterator.next();
                        if (!brick.isDestroyed() && brick.getRow() == rowToClear) {
                            brick.destroy();
                            onBrickDestroyed(brick);
                            clearedCount++;
                        }
                    }
                    GameLogger.debug("Cleared {} bricks from row {}", clearedCount, rowToClear);
                } else {
                    int colToClear = random.nextInt(maxCol + 1);
                    GameLogger.debug("Clearing column {} (max col: {})", colToClear, maxCol);
                    
                    double x = bricks.stream().filter(b -> b.getCol() == colToClear).findFirst().map(Brick::getX)
                            .orElse(0.0);
                    lineEffects.add(new LineEffect(x, 0, x, gameHeight, 0.5));
                    
                    // FIX: Use iterator instead of for-each to safely modify collection during iteration
                    Iterator<Brick> brickIterator = bricks.iterator();
                    int clearedCount = 0;
                    while (brickIterator.hasNext()) {
                        Brick brick = brickIterator.next();
                        if (!brick.isDestroyed() && brick.getCol() == colToClear) {
                            brick.destroy();
                            onBrickDestroyed(brick);
                            clearedCount++;
                        }
                    }
                    GameLogger.debug("Cleared {} bricks from column {}", clearedCount, colToClear);
                }
            }
            
            GameLogger.debug("Power-up effect applied successfully");
        } catch (ConcurrentModificationException e) {
            GameLogger.error(null, e);
        } catch (Exception e) {
            GameLogger.error("Exception in applyPowerUpEffect: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extracts the current game state for saving.
     * 
     * @return GameState DTO containing all necessary game data
     */
    public com.arkanoid.systems.save.GameState extractCurrentGameState() {
        // Extract paddle state
        Paddle paddle = player.getPaddle();
        PaddleState paddleState = new PaddleState(paddle.getX(),
                paddle.getY(), paddle.getWidth(), paddle.getHeight(), paddle.getVelocityX(), Paddle.getCurrentSkin(),
                paddle.isGunMode() ? "Gun" : null, paddle.isGunMode() ? paddle.getGunExpiry() : 0);

        // Extract ball states
        List<BallState> ballStates = new ArrayList<>();
        for (Ball ball : balls) {
            ballStates.add(new BallState(ball.getX(), ball.getY(), ball.getVelocityX(),
                    ball.getVelocityY(), ball.getRadius(), ball.isAttachedToPaddle(), Ball.getCurrentSkin()));
        }

        // Extract brick states
        List<BrickState> brickStates = new ArrayList<>();
        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                // We can't access protected hitPoints, so we estimate from hit() behavior
                int hitPoints = 1; // Default assumption for save/load
                String texturePath = null;
                if (brick instanceof BaseBrick) {
                    texturePath = ((BaseBrick) brick).getTexturePath();
                }
                brickStates.add(new BrickState(brick.getClass().getSimpleName(), brick.getX(),
                        brick.getY(), brick.getWidth(), brick.getHeight(), hitPoints, 0, // colorIndex - can be enhanced
                                                                                         // later
                        !brick.isDestroyed(), 0, // velocityX - for moving bricks if implemented
                        0, // velocityY
                        texturePath));
            }
        }

        // Extract power-up states
        List<PowerUpState> powerUpStates = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            powerUpStates.add(new PowerUpState(powerUp.getClass().getSimpleName(),
                    powerUp.getX(), powerUp.getY(), powerUp.getVelocityY(), powerUp.isActive()));
        }

        // Get elapsed time - for now we'll use 0, can be enhanced later with a timer
        int elapsedTimeSeconds = 0;

        return new com.arkanoid.systems.save.GameState(levelNumber, player.getState().getScore(),
                player.getState().getLives(), elapsedTimeSeconds, paddleState, ballStates, brickStates, powerUpStates);
    }

    /**
     * Restores the game state from a saved state.
     * 
     * @param gameState The saved game state to restore
     */
    public void restoreGameState(com.arkanoid.systems.save.GameState gameState) {
        // Validate positions are within bounds
        if (gameState == null || gameState.getPaddleState() == null || gameState.getBallStates() == null
                || gameState.getBrickStates() == null) {
            throw new IllegalStateException("Invalid game state: missing required data");
        }

        // Clear all visual effects to prevent trailing effects after load
        trailEffects.clear();

        int currentScore = player.getState().getScore();
        player.getState().addScore(-currentScore); // Reset to 0
        player.getState().addScore(gameState.getScore()); // Add saved score
        player.getState().setLives(gameState.getLives());

        // Restore paddle
        Paddle paddle = player.getPaddle();
        PaddleState paddleState = gameState.getPaddleState();
        paddle.setX(paddleState.x());
        paddle.setY(paddleState.y());
        paddle.setWidth(paddleState.width());
        paddle.setHeight(paddleState.height());
        paddle.setVelocityX(paddleState.velocityX());
        paddle.equipSkin(paddleState.equippedSkin());
        if ("Gun".equals(paddleState.activePowerUp())) {
            paddle.setGunMode(true);
            paddle.setGunExpiry(paddleState.powerUpExpiryNano());
        }

        // Restore balls
        balls.clear();
        for (BallState ballState : gameState.getBallStates()) {
            Ball ball = new Ball(ballState.x(), ballState.y(), ballState.radius(), 300);
            ball.setVelocityX(ballState.velocityX());
            ball.setVelocityY(ballState.velocityY());
            ball.setBounds(0, 0, gameWidth, gameHeight);
            ball.equipSkin(ballState.skin());
            if (!ballState.attachedToPaddle()) {
                ball.setAttachedToPaddle(false);
            }
            balls.add(ball);
        }

        // Restore bricks
        bricks.clear();
        for (BrickState brickState : gameState.getBrickStates()) {
            // Create brick based on type
            Brick brick = createBrickFromState(brickState);
            if (brick != null) {
                bricks.add(brick);
            }
        }

        // Restore power-ups
        powerUps.clear();
        for (PowerUpState powerUpState : gameState.getActivePowerUps()) {
            PowerUp powerUp = createPowerUpFromState(powerUpState);
            if (powerUp != null) {
                powerUps.add(powerUp);
            }
        }
    }

    private Brick createBrickFromState(BrickState brickState) {
        // Create brick based on type from state
        String type = brickState.type();
        int row = 0; // Default values since we don't have row/col in saved state
        int col = 0;
        String texturePath = brickState.texturePath();

        switch (type) {
        case "NormalBrick":
            return new NormalBrick(brickState.x(), brickState.y(), brickState.width(), brickState.height(), col, row,
                    texturePath);
        case "StrongBrick":
            return new StrongBrick(brickState.x(), brickState.y(), brickState.width(), brickState.height(), col, row,
                    texturePath);
        case "UnbreakableBrick":
            return new UnbreakableBrick(brickState.x(), brickState.y(), brickState.width(), brickState.height(), col,
                    row, texturePath);
        case "MovingBrick":
            return new MovingBrick(brickState.x(), brickState.y(), brickState.width(), brickState.height(), 0,
                    gameWidth, // minX, maxX
                    row, col, texturePath);
        default:
            return new NormalBrick(brickState.x(), brickState.y(), brickState.width(), brickState.height(), col, row,
                    texturePath);
        }
    }

    private PowerUp createPowerUpFromState(PowerUpState powerUpState) {
        // For now, return null - power-ups can be restored based on type
        // This would need to be enhanced based on actual PowerUp class hierarchy
        return null;
    }

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }
}
