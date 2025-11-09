package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.PowerUp;

/**
 * Service for managing power-ups in two-player mode.
 * Handles spawn, movement, and ownership logic.
 */
public interface PowerUpService {

    /**
     * Spawns a power-up at brick location with directional movement.
     * Power-up moves toward the player who broke the brick.
     * 
     * @param brickX            X position of destroyed brick
     * @param brickY            Y position of destroyed brick
     * @param ownerPlayerNumber Player who broke the brick (1 or 2)
     */
    void spawn(double brickX, double brickY, int ownerPlayerNumber);

    /**
     * Updates all active power-ups for the current frame.
     * Handles movement and collision detection.
     * 
     * @param deltaTime Time elapsed since last frame in seconds
     */
    void updateAll(double deltaTime);

    /**
     * Applies a power-up effect to the owning player only.
     * Filters out opponent-affecting power-ups for fairness.
     * 
     * @param playerNumber Player picking up the power-up (1 or 2)
     * @param powerUp      The power-up to apply
     */
    void applyPickup(int playerNumber, PowerUp powerUp);

    /**
     * Clears all active power-ups from the match.
     */
    void clear();
}
