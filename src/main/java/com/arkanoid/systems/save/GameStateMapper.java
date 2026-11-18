package com.arkanoid.systems.save;

import com.arkanoid.core.GameConstants;
import com.arkanoid.core.entities.*;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.Player;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps live game entities to and from serializable {@link GameState} DTOs.
 */
public final class GameStateMapper {
    private static final Logger logger = GameLogger.getLogger(GameStateMapper.class);
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;
    private static final long MAX_REASONABLE_DURATION_NANOS = 60_000_000_000L;

    private GameStateMapper() {
    }

    public static GameState toGameState(
            int levelNumber,
            double elapsedTimeSeconds,
            Player player,
            List<Ball> balls,
            List<Brick> bricks,
            List<PowerUp> powerUps) {
        Paddle paddle = player.getPaddle();
        long remainingGunNanos = paddle.isGunMode()
                ? Math.max(0L, Math.round(paddle.getGunTimerSeconds() * NANOS_PER_SECOND))
                : 0L;
        PaddleState paddleState = new PaddleState(
                paddle.getX(),
                paddle.getY(),
                paddle.getWidth(),
                paddle.getHeight(),
                paddle.getVelocityX(),
                paddle.getCurrentSkin(),
                paddle.isGunMode() ? "Gun" : null,
                remainingGunNanos
        );

        List<BallState> ballStates = new ArrayList<>(balls.size());
        for (Ball ball : balls) {
            String skin = ball.getEquippedSkin() != null ? ball.getEquippedSkin() : Ball.getCurrentSkin();
            ballStates.add(new BallState(
                    ball.getX(),
                    ball.getY(),
                    ball.getVelocityX(),
                    ball.getVelocityY(),
                    ball.getRadius(),
                    ball.isAttachedToPaddle(),
                    skin
            ));
        }

        List<BrickState> brickStates = new ArrayList<>();
        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                int hitPoints = (brick instanceof BaseBrick base) ? base.getHitPoints() : 1;
                String texturePath = (brick instanceof BaseBrick base) ? base.getTexturePath() : null;
                brickStates.add(new BrickState(
                        brick.getClass().getSimpleName(),
                        brick.getX(),
                        brick.getY(),
                        brick.getWidth(),
                        brick.getHeight(),
                        hitPoints,
                        0,
                        true,
                        0,
                        0,
                        texturePath
                ));
            }
        }

        List<PowerUpState> powerUpStates = new ArrayList<>(powerUps.size());
        for (PowerUp powerUp : powerUps) {
            powerUpStates.add(new PowerUpState(
                    powerUp.getClass().getSimpleName(),
                    powerUp.getX(),
                    powerUp.getY(),
                    powerUp.getVelocityY(),
                    powerUp.isActive()
            ));
        }

        return new GameState(
                levelNumber,
                player.getState().getScore(),
                player.getState().getLives(),
                (int) elapsedTimeSeconds,
                paddleState,
                ballStates,
                brickStates,
                powerUpStates
        );
    }

    public static double applyGameState(
            GameState gameState,
            double gameWidth,
            double gameHeight,
            Player player,
            List<Ball> balls,
            List<Brick> bricks,
            List<PowerUp> powerUps) {
        if (gameState == null || gameState.getPaddleState() == null
                || gameState.getBallStates() == null || gameState.getBrickStates() == null) {
            throw new IllegalStateException("Invalid game state: missing required data");
        }

        int currentScore = player.getState().getScore();
        player.getState().addScore(-currentScore);
        player.getState().addScore(gameState.getScore());
        player.getState().setLives(gameState.getLives());

        Paddle paddle = player.getPaddle();
        PaddleState paddleState = gameState.getPaddleState();
        paddle.setX(paddleState.x());
        paddle.setY(paddleState.y());
        paddle.setWidth(paddleState.width());
        paddle.setHeight(paddleState.height());
        paddle.setVelocityX(paddleState.velocityX());
        paddle.equipSkin(paddleState.equippedSkin());

        if ("Gun".equals(paddleState.activePowerUp())) {
            long storedNanos = paddleState.powerUpExpiryNano();
            double seconds = (storedNanos > 0 && storedNanos <= MAX_REASONABLE_DURATION_NANOS)
                    ? storedNanos / NANOS_PER_SECOND
                    : GameConstants.DEFAULT_POWERUP_DURATION_SECONDS;
            paddle.activateGun(seconds);
        } else {
            paddle.setGunMode(false);
        }

        balls.clear();
        for (BallState ballState : gameState.getBallStates()) {
            Ball ball = new Ball(ballState.x(), ballState.y(), ballState.radius(), GameConstants.DEFAULT_BALL_SPEED);
            ball.setVelocityX(ballState.velocityX());
            ball.setVelocityY(ballState.velocityY());
            ball.setBounds(0, 0, gameWidth, gameHeight);
            ball.equipSkin(ballState.skin());
            if (!ballState.attachedToPaddle()) {
                ball.setAttachedToPaddle(false);
            }
            balls.add(ball);
        }

        bricks.clear();
        for (BrickState brickState : gameState.getBrickStates()) {
            Brick brick = createBrickFromState(brickState, gameWidth);
            if (brick != null) {
                bricks.add(brick);
            }
        }

        powerUps.clear();
        if (gameState.getActivePowerUps() != null) {
            for (PowerUpState powerUpState : gameState.getActivePowerUps()) {
                PowerUp powerUp = createPowerUpFromState(powerUpState);
                if (powerUp != null) {
                    powerUps.add(powerUp);
                }
            }
        }

        return gameState.getElapsedTimeSeconds();
    }

    private static Brick createBrickFromState(BrickState brickState, double gameWidth) {
        String type = brickState.type();
        int row = 0;
        int col = 0;
        String texturePath = brickState.texturePath();

        return switch (type) {
            case "NormalBrick" -> new NormalBrick(brickState.x(), brickState.y(),
                    brickState.width(), brickState.height(), col, row, texturePath);
            case "StrongBrick" -> new StrongBrick(brickState.x(), brickState.y(),
                    brickState.width(), brickState.height(), col, row, texturePath);
            case "UnbreakableBrick" -> new UnbreakableBrick(brickState.x(), brickState.y(),
                    brickState.width(), brickState.height(), col, row, texturePath);
            case "MovingBrick" -> new MovingBrick(brickState.x(), brickState.y(),
                    brickState.width(), brickState.height(), 0, gameWidth, row, col, texturePath);
            default -> new NormalBrick(brickState.x(), brickState.y(),
                    brickState.width(), brickState.height(), col, row, texturePath);
        };
    }

    private static PowerUp createPowerUpFromState(PowerUpState powerUpState) {
        String type = powerUpState.type();
        double x = powerUpState.x();
        double y = powerUpState.y();

        PowerUp powerUp = switch (type) {
            case "ExplosiveBallPowerUp" -> new ExplosiveBallPowerUp(x, y);
            case "MultiBallPowerUp" -> new MultiBallPowerUp(x, y);
            case "ExpandPaddlePowerUp" -> new ExpandPaddlePowerUp(x, y);
            case "GunPaddlePowerUp" -> new GunPaddlePowerUp(x, y);
            case "RowClearPowerUp" -> new RowClearPowerUp(x, y);
            default -> {
                logger.warn("Unknown power-up type: {}, skipping", type);
                yield null;
            }
        };

        if (powerUp != null) {
            powerUp.setVelocityY(powerUpState.velocityY());
            if (!powerUpState.active()) {
                powerUp.setActive(false);
            }
        }
        return powerUp;
    }
}
