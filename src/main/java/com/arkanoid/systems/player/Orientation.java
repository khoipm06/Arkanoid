package com.arkanoid.systems.player;

/**
 * Represents the positional orientation of a player in a two-player match.
 * Used to determine paddle/ball positioning and launch direction.
 */
public enum Orientation {
    /**
     * Bottom orientation - paddle at bottom of screen, ball launches upward.
     * Dead Side drain occurs when ball crosses maxY boundary.
     */
    BOTTOM(1),

    /**
     * Top orientation - paddle at top of screen, ball launches downward.
     * Dead Side drain occurs when ball crosses minY boundary.
     */
    TOP(-1);

    private final int directionMultiplier;

    Orientation(int directionMultiplier) {
        this.directionMultiplier = directionMultiplier;
    }

    /**
     * Returns the direction multiplier for launch velocity.
     * BOTTOM returns 1 (upward), TOP returns -1 (downward).
     */
    public int getDirectionMultiplier() {
        return directionMultiplier;
    }

    /**
     * Gets the appropriate Orientation for a given player number.
     * 
     * @param playerNumber 1 for bottom player, 2 for top player
     * @return BOTTOM for player 1, TOP for player 2
     */
    public static Orientation fromPlayerNumber(int playerNumber) {
        return playerNumber == 1 ? BOTTOM : TOP;
    }
}
