package com.arkanoid.systems.input;

import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.player.Player;
import com.arkanoid.systems.twoplayer.TwoPlayerMatchManager;
import javafx.scene.input.KeyCode;

import java.util.Set;

/**
 * Simple input handler for two-player mode.
 * Handles input for both players and game controls.
 */
public class TwoPlayerInputHandler extends BaseInputHandler {
    private final Player player1;
    private final Player player2;
    private final TwoPlayerMatchManager matchManager;
    private Runnable onPauseCallback;
    private Runnable onResumeCallback;
    
    // Player 1 controls: A/D
    private static final KeyCode PLAYER1_LEFT = KeyCode.A;
    private static final KeyCode PLAYER1_RIGHT = KeyCode.D;
    
    // Player 2 controls: Arrow keys
    private static final KeyCode PLAYER2_LEFT = KeyCode.LEFT;
    private static final KeyCode PLAYER2_RIGHT = KeyCode.RIGHT;

    public TwoPlayerInputHandler(Player player1, Player player2, TwoPlayerMatchManager matchManager) {
        this.player1 = player1;
        this.player2 = player2;
        this.matchManager = matchManager;
    }

    public void setOnPauseCallback(Runnable callback) {
        this.onPauseCallback = callback;
    }

    public void setOnResumeCallback(Runnable callback) {
        this.onResumeCallback = callback;
    }

    @Override
    public boolean handleKeyPress(KeyCode key) {
        if (isSpaceKey(key)) {
            launchAllBalls();
            return true;
        }

        if (isPauseKey(key)) {
            togglePause();
            return true;
        }

        return false;
    }

    /**
     * Handles continuous input (movement keys)
     */
    public void handleContinuousInput(Set<KeyCode> activeKeys, double deltaTime) {
        if (matchManager.getState() != TwoPlayerMatchManager.MatchState.PLAYING) {
            return;
        }

        // Player 1 movement
        Paddle paddle1 = player1.getPaddle();
        if (activeKeys.contains(PLAYER1_LEFT)) {
            paddle1.moveLeft(deltaTime);
        }
        if (activeKeys.contains(PLAYER1_RIGHT)) {
            paddle1.moveRight(deltaTime);
        }

        // Player 2 movement
        Paddle paddle2 = player2.getPaddle();
        if (activeKeys.contains(PLAYER2_LEFT)) {
            paddle2.moveLeft(deltaTime);
        }
        if (activeKeys.contains(PLAYER2_RIGHT)) {
            paddle2.moveRight(deltaTime);
        }
    }

    /**
     * Launches all balls for both players
     */
    private void launchAllBalls() {
        launchBall(player1.getBall());
        launchBall(player2.getBall());
    }

    private void togglePause() {
        if (matchManager.getState() == TwoPlayerMatchManager.MatchState.PLAYING) {
            matchManager.pause();
            if (onPauseCallback != null) {
                onPauseCallback.run();
            }
        } else if (matchManager.getState() == TwoPlayerMatchManager.MatchState.PAUSED) {
            matchManager.resume();
            if (onResumeCallback != null) {
                onResumeCallback.run();
            }
        }
    }

    public TwoPlayerMatchManager getMatchManager() {
        return matchManager;
    }
}
