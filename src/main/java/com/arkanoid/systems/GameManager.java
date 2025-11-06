package com.arkanoid.systems;

import com.arkanoid.core.entities.*;
import com.arkanoid.core.physics.CollisionDetector;
import com.arkanoid.systems.level.LevelManager;
import com.arkanoid.systems.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static final double GUN_FIRE_INTERVAL = 0.2; // 0.2s ~ 5 viên/s
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

    private final List<Bullet> bullets;
    private double gunFireCooldown = 0.0; // thời gian đếm ngược tới lần bắn tiếp theo (giây)
    public GameManager(double gameWidth, double gameHeight, int levelNumber) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.currentState = GameState.MENU;
        this.levelManager = new LevelManager();
        this.playerManager = new PlayerManager();
        this.balls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.lineEffects = new ArrayList<>();
        this.levelNumber = levelNumber;
        this.bricks = levelManager.loadLevel(levelNumber);
        Paddle paddle = new Paddle(gameWidth / 2 - 30, gameHeight - 30, 100, 25, 400, 0, gameWidth);
        player = new Player("Player1", 1, paddle);
        playerManager.addPlayer(1, player);
    }

    public void startGame() {
        currentState = GameState.PLAYING;

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
        String mapPath = "/levels/level" + levelNumber + ".json";
        bricks = levelManager.loadLevelFromFile(mapPath);
        powerUps.clear();
        bullets.clear();
    }

    public void update(double deltaTime) {
        if (currentState != GameState.PLAYING) return;

        playerManager.update(deltaTime);
        Paddle paddle = player.getPaddle();

        for (Ball ball : balls) {
            if (ball.isAttachedToPaddle()) {
                ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getRadius());
                ball.setY(paddle.getY() - ball.getRadius() * 2);
            }
            ball.update(deltaTime);
            ball.checkPaddleCollision(paddle);
        }

        for (Brick brick : bricks) {
            brick.update(deltaTime);
        }

        for (Ball ball : balls) {
            CollisionDetector.checkBallBrickCollisions(ball, bricks, this::onBrickDestroyed, this);
        }

        List<PowerUp> toRemovePowerUps = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);

            if (powerUp.checkPaddleCollision(paddle)) {
                player.getState().addScore(50);
                applyPowerUpEffect(powerUp, paddle);
                toRemovePowerUps.add(powerUp);
            } else if (!powerUp.isActive() || powerUp.getY() > gameHeight) {
                toRemovePowerUps.add(powerUp);
            }
        }
        powerUps.removeAll(toRemovePowerUps);
        bricks.removeIf(Brick::isDestroyed);

        List<Ball> ballsToRemove = new ArrayList<>();
        for (Ball ball : balls) {
            if (ball.isOutOfBounds()) {
                player.getState().loseLife();
                if (player.getState().isGameOver()) {
                    currentState = GameState.GAME_OVER;
                }
                ballsToRemove.add(ball);
            }
        }
        balls.removeAll(ballsToRemove);

        if (balls.isEmpty() && currentState == GameState.PLAYING) {
            resetBall(paddle);
        }

        if (bricks.isEmpty()) {
            currentState = GameState.LEVEL_COMPLETE;
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
                // spawn 2 bullets (left & right)
                double bulletW = 6;
                double bulletH = 12;
                double speed = -300; // pixels / s (bay lên)
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
                if (brick.isDestroyed()) continue;

                if (b.intersects((com.arkanoid.core.entities.GameObject) brick)) {
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
        bricks.removeIf(Brick::isDestroyed);
        if (bricks.isEmpty()) {
            currentState = GameState.LEVEL_COMPLETE;
        }
    }

    public List<LineEffect> getLineEffects() {
        return lineEffects;
    }

    public void addExplosion(double x, double y, double radius, double duration) {
        explosions.add(new Explosion(x, y, radius, duration));
    }

    private void onBrickDestroyed(Brick brick) {
        if (brick.isDestroyed()) {
            player.getState().addScore(100);
            PowerUp powerUp = brick.dropPowerUp();
            if (powerUp != null) {
                powerUps.add(powerUp);
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

    //    public int getHighestScore() {
//        return playerManager.getHighestScore();
//    }
    private void applyPowerUpEffect(PowerUp powerUp, Paddle paddle) {
        if (powerUp instanceof ExplosiveBallPowerUp) {
            for (Ball ball : balls) {
                ball.setExplosive(true);
                ball.setHasExploded(false);
            }
        } else if (powerUp instanceof RowClearPowerUp) {
            Random random = new Random();
            boolean clearRow = random.nextBoolean();

            // tìm max row và max col
            int maxRow = -1, maxCol = -1;
            for (Brick b : bricks) {
                if (!b.isDestroyed()) {
                    maxRow = Math.max(maxRow, b.getRow());
                    maxCol = Math.max(maxCol, b.getCol());
                }
            }
            if (maxRow < 0 || maxCol < 0) return; // không có brick nào

            List<Brick> bricksToDestroy = new ArrayList<>();
            if (clearRow) {
                int rowToClear = random.nextInt(maxRow + 1);
                for (Brick b : bricks) {
                    if (!b.isDestroyed() && b.getRow() == rowToClear && !(b instanceof UnbreakableBrick)) {
                        bricksToDestroy.add(b);
                    }
                }
                if (bricksToDestroy.isEmpty()) return;

                // vẽ lineEffect
                double y = bricksToDestroy.get(0).getY();
                lineEffects.add(new LineEffect(0, y, gameWidth, y, 0.5));

            } else {
                int colToClear = random.nextInt(maxCol + 1);
                for (Brick b : bricks) {
                    if (!b.isDestroyed() && b.getCol() == colToClear && !(b instanceof UnbreakableBrick)) {
                        bricksToDestroy.add(b);
                    }
                }
                if (bricksToDestroy.isEmpty()) return;

                // vẽ lineEffect
                double x = bricksToDestroy.get(0).getX();
                lineEffects.add(new LineEffect(x, 0, x, gameHeight, 0.5));
            }

            // destroy bricks mà không sửa list gốc ngay lập tức
            List<Brick> toRemove = new ArrayList<>();
            for (Brick b : bricksToDestroy) {
                if (!b.isDestroyed()) {
                    b.destroy();
                    onBrickDestroyed(b);
                    toRemove.add(b); // Thêm vào list tạm thời để xóa khỏi list chính
                }
            }
            bricks.removeAll(toRemove);

        }
    }

    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }

}

