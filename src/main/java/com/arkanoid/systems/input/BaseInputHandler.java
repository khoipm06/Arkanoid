package com.arkanoid.systems.input;

import com.arkanoid.core.entities.Ball;
import javafx.scene.input.KeyCode;

import java.util.Set;

/**
 * Base class for input handlers with common functionality.
 * Provides shared methods for key handling and ball launching.
 */
public abstract class BaseInputHandler {
    
    /**
     * Handles a single key press action.
     * 
     * @param key The key code that was pressed
     * @return true if the key was handled, false otherwise
     */
    public abstract boolean handleKeyPress(KeyCode key);
    
    /**
     * Handles continuous input (movement keys that are held down).
     * 
     * @param pressedKeys Set of currently pressed keys
     * @param deltaTime Time elapsed since last frame
     */
    public abstract void handleContinuousInput(Set<KeyCode> pressedKeys, double deltaTime);
    
    /**
     * Launches a ball if it's attached to a paddle.
     * 
     * @param ball The ball to launch
     * @return true if the ball was launched, false if it was already launched
     */
    protected boolean launchBall(Ball ball) {
        if (ball != null && ball.isAttachedToPaddle()) {
            ball.launch();
            return true;
        }
        return false;
    }
    
    /**
     * Handles the space key press to launch balls.
     * 
     * @param key The key code to check
     * @return true if space was pressed, false otherwise
     */
    protected boolean isSpaceKey(KeyCode key) {
        return key == KeyCode.SPACE;
    }
    
    /**
     * Handles pause/escape key press.
     * 
     * @param key The key code to check
     * @return true if pause key was pressed, false otherwise
     */
    protected boolean isPauseKey(KeyCode key) {
        return key == KeyCode.ESCAPE || key == KeyCode.P;
    }
}
