package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.player.Orientation;
import com.arkanoid.systems.player.Player;

/**
 * Implementation of RespawnService for two-player mode.
 * Handles immediate ball respawn with auto-launch behavior.
 */
public class RespawnServiceImpl implements RespawnService {

    private final Player player1;
    private final Player player2;
    private final double launchSpeed;

    public RespawnServiceImpl(Player player1, Player player2, double launchSpeed) {
        this.player1 = player1;
        this.player2 = player2;
        this.launchSpeed = launchSpeed;
    }

    @Override
    public void respawnBall(int playerNumber) {
        Player player = (playerNumber == 1) ? player1 : player2;
        Ball ball = player.getBall();
        Paddle paddle = player.getPaddle();
        Orientation orientation = player.getOrientation();

        if (ball == null || paddle == null) {
            return;
        }

        // Center ball on paddle
        double ballX = paddle.getCenterX() - ball.getRadius();
        double ballY;

        // Position based on orientation
        if (orientation == Orientation.BOTTOM) {
            ballY = paddle.getY() - ball.getHeight();
        } else {
            ballY = paddle.getY() + paddle.getHeight();
        }

        ball.setX(ballX);
        ball.setY(ballY);

        // Auto-launch away from Dead Side
        int direction = orientation.getDirectionMultiplier();
        ball.setVelocityX(0);
        ball.setVelocityY(-direction * launchSpeed); // BOTTOM: upward (-1 * 1), TOP: downward (-1 * -1)

        // Ensure ball is not attached to paddle
        ball.setAttachedToPaddle(false);

        System.out.println("Player " + playerNumber + " ball respawned and auto-launched (" + orientation + ")");
    }

    @Override
    public double getLaunchSpeed() {
        return launchSpeed;
    }
}
