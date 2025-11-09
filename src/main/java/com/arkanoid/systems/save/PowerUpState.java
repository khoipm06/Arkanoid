package com.arkanoid.systems.save;

/**
 * Record representing the state of a power-up for save/load operations.
 *
 * @param type      Power-up type (e.g., "ExpandPaddle", "MultiBall",
 *                  "ExtraLife")
 * @param x         X position on canvas
 * @param y         Y position on canvas
 * @param velocityY Vertical falling velocity
 * @param active    Whether power-up is active/falling
 */
public record PowerUpState(
        String type,
        double x,
        double y,
        double velocityY,
        boolean active) {
}
