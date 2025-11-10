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
    public enum GameState {
        MENU, PLAYING, PAUSED, GAME_OVER, LEVEL_COMPLETE
    }

    private GameState currentState;
    private LevelManager levelManager;
    private PlayerManager playerManager;
    private List<Ball> balls;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private List<Explosion> explosions;
    private List<LineEffect> lineEffects;
    private Player player;
    private double gameWidth;
    private double gameHeight;

    public GameManager(double gameWidth, double gameHeight) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.currentState = GameState.MENU;
        this.levelManager = new LevelManager();
        this.playerManager = new PlayerManager();
        this.balls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.lineEffects = new ArrayList<>();
        this.levelManager = new LevelManager();
        this.levelNumber = levelNumber;
        this.bricks = levelManager.loadLevel(levelNumber);
        Paddle paddle = new Paddle(gameWidth / 2 - 30, gameHeight - 30, 100, 25, 400, 0, gameWidth);
        player = new Player("Player1", 1, paddle);
        playerManager.addPlayer(1, player);
    }

    public void startGame() {
        currentState = GameState.PLAYING;
        
        Paddle paddle = new Paddle(gameWidth / 2 - 50, gameHeight - 50, 100, 15, 400, 0, gameWidth);
        player = new Player("Player1", 1, paddle);
        playerManager.addPlayer(1, player);
        
        Ball ball = new Ball(gameWidth / 2, gameHeight - 100, 8, 300);
        ball.setBounds(0, 0, gameWidth, gameHeight);
        balls.add(ball);
        
        loadLevel(1);
    }

    public void loadLevel(int levelNumber) {
        bricks = levelManager.loadLevel(levelNumber);
        powerUps.clear();
    }

    public void update(double deltaTime) {
        if (currentState != GameState.PLAYING) return;

        playerManager.update(deltaTime);

        for (Ball ball : balls) {
            ball.update(deltaTime);
            ball.checkPaddleCollision(player.getPaddle());
        }

        for (Brick brick : bricks) {
            brick.update(deltaTime);
        }

        for (Ball ball : balls) {
            CollisionDetector.checkBallBrickCollisions(ball, bricks, this::onBrickDestroyed, this);
        }

        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.update(deltaTime);

            PowerUp collectedPowerUp = powerUp.checkPaddleCollision(player.getPaddle());
            if (collectedPowerUp != null) {
                player.getState().addScore(50);
                applyPowerUpEffect(collectedPowerUp, player.getPaddle());
            }

            if (!powerUp.isActive() || powerUp.getY() > gameHeight) {
                powerUpIterator.remove();
            }
        }

        bricks.removeIf(Brick::isDestroyed);

        balls.removeIf(ball -> {
            if (ball.isOutOfBounds()) {
                player.getState().loseLife();
                if (player.getState().isGameOver()) {
                    currentState = GameState.GAME_OVER;
                }
                return true;
            }
            return false;
        });

        if (balls.isEmpty() && currentState == GameState.PLAYING) {
            resetBall();
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

    private void resetBall() {
        Ball ball = new Ball(gameWidth / 2, gameHeight - 100, 8, 300);
        ball.setBounds(0, 0, gameWidth, gameHeight);
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

    public GameState getCurrentState() { return currentState; }
    public void setCurrentState(GameState state) { this.currentState = state; }
    public List<Ball> getBalls() { return balls; }
    public List<Brick> getBricks() { return bricks; }
    public List<PowerUp> getPowerUps() { return powerUps; }
    public List<Explosion> getExplosions() { return explosions; }
    public Player getPlayer() { return player; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public int getLevelNumber() {
        return levelNumber;
    }
    public int getScore() {
        return player.getState().getScore();
    }

//    public int getHighestScore() {
//        return playerManager.getHighestScore();
//    }

    private void applyPowerUpEffect(PowerUp powerUp, Paddle paddle) {
        if (powerUp instanceof ExplosiveBallPowerUp) {
            for (Ball ball : balls) {
                ball.setExplosive(true);
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
                double y = bricks.stream().filter(b -> b.getRow() == rowToClear).findFirst().map(Brick::getY).orElse(0.0);
                lineEffects.add(new LineEffect(0, y, gameWidth, y, 0.5));
                for (Brick brick : bricks) {
                    if (!brick.isDestroyed() && brick.getRow() == rowToClear) {
                        brick.destroy();
                        onBrickDestroyed(brick);
                    }
                }
            } else {
                int colToClear = random.nextInt(maxCol + 1);
                double x = bricks.stream().filter(b -> b.getCol() == colToClear).findFirst().map(Brick::getX).orElse(0.0);
                lineEffects.add(new LineEffect(x, 0, x, gameHeight, 0.5));
                for (Brick brick : bricks) {
                    if (!brick.isDestroyed() && brick.getCol() == colToClear) {
                        brick.destroy();
                        onBrickDestroyed(brick);
                    }
                }
            }
        }
    }

}
