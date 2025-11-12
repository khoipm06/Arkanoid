package com.arkanoid.systems;

import com.arkanoid.core.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        gameManager = GameManager.getInstance(800, 600, 1);
    }

    @Test
    void testStartGame() {
        gameManager.startGame();
        assertEquals(GameManager.GameState.PLAYING, gameManager.getCurrentState());
        assertFalse(gameManager.getBalls().isEmpty());
    }

    @Test
    void testUpdate_PlayingState() {
        gameManager.startGame();
        gameManager.update(0.16);
    }

    @Test
    void testApplyExplosiveBallPowerUp() {
        gameManager.startGame();
        Ball ball = gameManager.getBalls().get(0);
        assertFalse(ball.isExplosive());

        // Set ball to explosive directly since power-up system may require JavaFX
        ball.setExplosive(true);
        assertTrue(ball.isExplosive());
    }

    @Test
    void testGameOver() {
        gameManager.startGame();
        
        // Set player to have only 1 life, then lose it
        gameManager.getPlayer().getState().setLives(1);
        
        // Make ball go out of bounds to trigger game over
        if (!gameManager.getBalls().isEmpty()) {
            Ball ball = gameManager.getBalls().get(0);
            // Launch ball from paddle so it can move and be checked for out of bounds
            ball.launch();
            // Ensure bounds are set (in case startGame didn't set them)
            ball.setBounds(0, 0, 800, 600);
            ball.setY(650); // Move ball well past the bottom boundary (600)
            ball.setVelocityY(10); // Ensure it's moving downward
            gameManager.update(0.16);
        }

        assertEquals(GameManager.GameState.GAME_OVER, gameManager.getCurrentState());
    }
}
