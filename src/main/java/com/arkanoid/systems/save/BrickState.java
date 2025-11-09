package com.arkanoid.systems.save;

/**
 * Record representing the state of a brick for save/load operations.
 *
 * @param type               Brick type (e.g., "Normal", "Strong",
 *                           "Unbreakable", "Moving")
 * @param x                  X position on canvas
 * @param y                  Y position on canvas
 * @param width              Brick width
 * @param height             Brick height
 * @param hitPointsRemaining HP left (0 = destroyed)
 * @param colorIndex         Visual color index
 * @param visible            Visibility flag
 * @param velocityX          Horizontal velocity (for moving bricks)
 * @param velocityY          Vertical velocity (for moving bricks)
 * @param texturePath        Path to brick texture image
 */
public record BrickState(
        String type,
        double x,
        double y,
        double width,
        double height,
        int hitPointsRemaining,
        int colorIndex,
        boolean visible,
        double velocityX,
        double velocityY,
        String texturePath) {
}
