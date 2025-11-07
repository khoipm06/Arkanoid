package com.arkanoid.systems;

import com.arkanoid.core.entities.*;
import com.arkanoid.systems.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        gameManager = new GameManager(800, 600, 1);
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
        // More assertions can be added here to check the state after update
    }

    @Test
    void testApplyExplosiveBallPowerUp() {
        gameManager.startGame();
        Ball ball = gameManager.getBalls().get(0);
        assertFalse(ball.isExplosive());

        ExplosiveBallPowerUp powerUp = new ExplosiveBallPowerUp(0, 0);
        gameManager.getPlayer().getPaddle().setY(100); // Move paddle to avoid immediate collection
        powerUp.setY(100);
        powerUp.checkPaddleCollision(gameManager.getPlayer().getPaddle());

        // This test is not ideal as it depends on the internal implementation of applyPowerUpEffect
        // A better approach would be to have a direct way to apply powerups for testing
    }

    @Test
    void testGameOver() throws NoSuchFieldException, IllegalAccessException {
        gameManager.startGame();
        Player player = gameManager.getPlayer();

        // Use reflection to set lives to 1
        java.lang.reflect.Field livesField = player.getState().getClass().getDeclaredField("lives");
        livesField.setAccessible(true);
        livesField.set(player.getState(), 1);

        // Simulate ball going out of bounds
        gameManager.getBalls().get(0).setY(700);
        gameManager.update(0.16);

        assertEquals(GameManager.GameState.GAME_OVER, gameManager.getCurrentState());
    }
}
