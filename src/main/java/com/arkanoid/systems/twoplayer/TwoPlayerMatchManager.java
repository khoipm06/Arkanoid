package com.arkanoid.systems.twoplayer;

/**
 * Manages a two-player competitive match session.
 * Coordinates game state, win conditions, and player interactions.
 */
public interface TwoPlayerMatchManager {

    /**
     * Match state enumeration.
     */
    enum MatchState {
        READY, // Match initialized, waiting for start
        PLAYING, // Active gameplay
        PAUSED, // Temporarily paused
        GAME_OVER // Match finished
    }

    /**
     * Reasons for match ending.
     */
    enum EndReason {
        SCORE_REACHED, // A player reached 10,000 points
        LIVES_DEPLETED, // A player lost all lives
        BRICKS_CLEARED, // All bricks destroyed
        DRAW // Tie condition
    }

    /**
     * Causes of life loss.
     */
    enum LifeLossCause {
        DEAD_SIDE, // Ball crossed Dead Side boundary
        OPPONENT_BALL // Opponent's ball hit paddle
    }

    /**
     * Starts the match and transitions to PLAYING state.
     */
    void startMatch();

    /**
     * Updates the match state for the current frame.
     * 
     * @param deltaTime Time elapsed since last frame in seconds
     */
    void update(double deltaTime);

    /**
     * Applies score for a brick hit to the appropriate player.
     * 
     * @param playerNumber Player who hit the brick (1 or 2)
     * @param brickValue   Point value of the brick
     */
    void applyBrickHit(int playerNumber, int brickValue);

    /**
     * Handles life loss event for a player.
     * 
     * @param playerNumber Player who lost a life (1 or 2)
     * @param cause        The cause of the life loss
     */
    void handleLifeLoss(int playerNumber, LifeLossCause cause);

    /**
     * Ends the match with the specified reason.
     * 
     * @param reason Why the match ended
     */
    void endMatch(EndReason reason);

    /**
     * Gets the current match state.
     * 
     * @return Current MatchState
     */
    MatchState getState();

    /**
     * Pauses the match.
     */
    void pause();

    /**
     * Resumes the match from paused state.
     */
    void resume();
}
