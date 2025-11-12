package com.arkanoid.systems.input;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.systems.GameManager;
import javafx.scene.input.KeyCode;

import java.util.Set;

/**
 * Simple input handler that decouples input processing from UI components.
 * Handles game-related key presses and delegates to appropriate game systems.
 */
public class InputHandler extends BaseInputHandler {
    private final GameManager gameManager;

    public InputHandler(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean handleKeyPress(KeyCode key) {
        if (isPauseKey(key)) {
            gameManager.togglePause();
            return true;
        }

        if (isSpaceKey(key)) {
            launchAllBalls();
            return true;
        }

        return false;
    }

    @Override
    public void handleContinuousInput(Set<KeyCode> pressedKeys, double deltaTime) {
        if (gameManager.getCurrentState() != GameManager.GameState.PLAYING) {
            return;
        }

        for (KeyCode key : pressedKeys) {
            gameManager.getPlayerManager().handleInput(key, true, deltaTime);
        }
    }

    /**
     * Launches all balls that are attached to paddles
     */
    private void launchAllBalls() {
        for (Ball ball : gameManager.getBalls()) {
            launchBall(ball);
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
