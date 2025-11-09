package com.arkanoid.systems.save;

/**
 * Record representing the state of a ball for save/load operations.
 *
 * @param x                X position on canvas
 * @param y                Y position on canvas
 * @param velocityX        Horizontal velocity
 * @param velocityY        Vertical velocity
 * @param radius           Ball radius
 * @param attachedToPaddle If true, ball launches on next space press
 * @param skin             Ball skin identifier (e.g., "Default", "Fire", "Ice")
 */
public record BallState(
        double x,
        double y,
        double velocityX,
        double velocityY,
        double radius,
        boolean attachedToPaddle,
        String skin) {
}
