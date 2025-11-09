package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.player.Player;

/**
 * Implementation of CollisionService for two-player mode.
 * Handles ball-ball elastic collisions and opponent paddle detection.
 */
public class CollisionServiceImpl implements CollisionService {

    private final Player player1;
    private final Player player2;

    public CollisionServiceImpl(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public boolean checkBallBallCollision(Ball ball1, Ball ball2) {
        // Bounding circle collision detection
        double dx = ball1.getCenterX() - ball2.getCenterX();
        double dy = ball1.getCenterY() - ball2.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double radiusSum = ball1.getRadius() + ball2.getRadius();

        return distance < radiusSum;
    }

    @Override
    public void resolveBallBallCollision(Ball ball1, Ball ball2) {
        // Calculate collision normal (from ball1 to ball2)
        double dx = ball2.getCenterX() - ball1.getCenterX();
        double dy = ball2.getCenterY() - ball1.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) {
            return; // Prevent division by zero
        }

        // Normalize collision normal
        double normalX = dx / distance;
        double normalY = dy / distance;

        // Get current velocities
        double v1x = ball1.getVelocityX();
        double v1y = ball1.getVelocityY();
        double v2x = ball2.getVelocityX();
        double v2y = ball2.getVelocityY();

        // Calculate relative velocity
        double relativeVx = v1x - v2x;
        double relativeVy = v1y - v2y;

        // Calculate relative velocity along collision normal
        double velocityAlongNormal = relativeVx * normalX + relativeVy * normalY;

        // Don't resolve if balls are moving apart
        if (velocityAlongNormal > 0) {
            return;
        }

        // Elastic collision: swap velocity components along normal
        // Simplified approach for equal mass objects
        double impulse = velocityAlongNormal;

        // Update velocities
        ball1.setVelocityX(v1x - impulse * normalX);
        ball1.setVelocityY(v1y - impulse * normalY);
        ball2.setVelocityX(v2x + impulse * normalX);
        ball2.setVelocityY(v2y + impulse * normalY);

        // Separate balls to prevent overlap
        double overlap = (ball1.getRadius() + ball2.getRadius()) - distance;
        if (overlap > 0) {
            double separationX = normalX * overlap * 0.5;
            double separationY = normalY * overlap * 0.5;

            ball1.setX(ball1.getX() - separationX);
            ball1.setY(ball1.getY() - separationY);
            ball2.setX(ball2.getX() + separationX);
            ball2.setY(ball2.getY() + separationY);
        }
    }

    @Override
    public boolean checkOpponentPaddleHit(Ball ball, int playerNumber) {
        // Get opponent's paddle
        Paddle opponentPaddle = (playerNumber == 1) ? player2.getPaddle() : player1.getPaddle();

        // Check if ball intersects with opponent paddle
        return ball.intersects(opponentPaddle);
    }
}
