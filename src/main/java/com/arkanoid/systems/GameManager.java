package com.arkanoid.systems;

import com.arkanoid.core.entities.*;
import com.arkanoid.core.physics.CollisionDetector;
import com.arkanoid.systems.level.LevelManager;
import com.arkanoid.systems.player.Player;
import com.arkanoid.systems.sound.SoundManager;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private Player player;
    private double gameWidth;
    private double gameHeight;
    private int levelNumber;

    public GameManager(double gameWidth, double gameHeight, int levelNumber) {
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
        this.currentState = GameState.MENU;
        this.levelManager = new LevelManager();
        this.playerManager = new PlayerManager();
        this.balls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.levelManager = new LevelManager();
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
            CollisionDetector.checkBallBrickCollisions(ball, bricks, this::onBrickDestroyed);
        }

        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            powerUp.update(deltaTime);
            
            if (powerUp.checkPaddleCollision(player.getPaddle())) {
                player.getState().addScore(50);
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
            resetBall(paddle);
        }

        if (bricks.isEmpty()) {
            currentState = GameState.LEVEL_COMPLETE;
        }
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
    public Player getPlayer() { return player; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public int getLevelNumber() {
        return levelNumber;
    }

}
