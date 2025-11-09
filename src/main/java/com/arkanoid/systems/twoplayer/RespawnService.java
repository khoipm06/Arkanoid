package com.arkanoid.systems.twoplayer;

/**
 * Service for handling ball respawn logic in two-player mode.
 * Manages immediate respawn with auto-launch behavior.
 */
public interface RespawnService {

    /**
     * Respawns a player's ball after life loss.
     * Centers ball on paddle and auto-launches away from Dead Side.
     * 
     * @param playerNumber Player whose ball to respawn (1 or 2)
     */
    void respawnBall(int playerNumber);

    /**
     * Gets the initial launch speed for respawned balls.
     * 
     * @return Launch speed in pixels per second
     */
    double getLaunchSpeed();
}
