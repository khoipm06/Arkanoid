package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.Ball;

/**
 * Service for detecting and resolving collisions in two-player mode.
 * Handles ball-ball collisions and opponent paddle interactions.
 */
public interface CollisionService {

    /**
     * Checks if two balls are colliding.
     * 
     * @param ball1 First ball
     * @param ball2 Second ball
     * @return true if balls are overlapping
     */
    boolean checkBallBallCollision(Ball ball1, Ball ball2);

    /**
     * Resolves elastic collision between two balls.
     * Updates velocities of both balls using physics reflection.
     * 
     * @param ball1 First ball
     * @param ball2 Second ball
     */
    void resolveBallBallCollision(Ball ball1, Ball ball2);

    /**
     * Checks if a ball hit the opponent's paddle.
     * Used to detect life loss condition.
     * 
     * @param ball         Ball to check
     * @param playerNumber The owner of the ball (1 or 2)
     * @return true if opponent paddle was hit
     */
    boolean checkOpponentPaddleHit(Ball ball, int playerNumber);
}
