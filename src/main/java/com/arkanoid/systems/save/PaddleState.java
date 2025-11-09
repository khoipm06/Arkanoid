package com.arkanoid.systems.save;

/**
 * Record representing the state of a paddle for save/load operations.
 *
 * @param x                 X position on canvas
 * @param y                 Y position on canvas
 * @param width             Paddle width
 * @param height            Paddle height
 * @param velocityX         Current horizontal velocity
 * @param equippedSkin      Skin identifier (e.g., "paddle_Default",
 *                          "paddle_Metal")
 * @param activePowerUp     Active power-up type or null
 * @param powerUpExpiryNano Nano time when power-up expires (0 if none)
 */
public record PaddleState(
        double x,
        double y,
        double width,
        double height,
        double velocityX,
        String equippedSkin,
        String activePowerUp,
        long powerUpExpiryNano) {
}
