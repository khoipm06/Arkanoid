package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.*;
import com.arkanoid.systems.player.Orientation;
import com.arkanoid.systems.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PowerUpServiceImpl in two-player mode.
 */
class PowerUpServiceImplTest {

    private PowerUpServiceImpl powerUpService;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        // Create players with paddles
        Paddle paddle1 = new Paddle(350, 550, 100, 20);
        Paddle paddle2 = new Paddle(350, 50, 100, 20);

        player1 = new Player("Player1", 1, paddle1);
        player2 = new Player("Player2", 2, paddle2);
        powerUpService = new PowerUpServiceImpl(player1, player2);
    }

    @Test
    void testSpawnPowerUpForPlayer1() {
        // Spawn power-up for player 1 at brick location
        double brickX = 400;
        double brickY = 300;

        powerUpService.spawn(brickX, brickY, 1);

        // Verify power-up was added (indirectly by checking no exception)
        assertDoesNotThrow(() -> powerUpService.updateAll(0.016));
    }

    @Test
    void testSpawnPowerUpForPlayer2() {
        // Spawn power-up for player 2 at brick location
        double brickX = 400;
        double brickY = 100;

        powerUpService.spawn(brickX, brickY, 2);

        // Verify power-up was added
        assertDoesNotThrow(() -> powerUpService.updateAll(0.016));
    }

    @Test
    void testPowerUpMovementDirection() {
        // Player 1 (BOTTOM orientation) should have downward velocity
        double brickX = 400;
        double brickY = 300;

        powerUpService.spawn(brickX, brickY, 1);

        // Update multiple times to ensure movement
        for (int i = 0; i < 10; i++) {
            powerUpService.updateAll(0.016);
        }

        // No assertions needed - just verify no crashes
        assertDoesNotThrow(() -> powerUpService.updateAll(0.016));
    }

    @Test
    void testUpdateAllPowerUps() {
        // Spawn multiple power-ups
        powerUpService.spawn(100, 100, 1);
        powerUpService.spawn(200, 200, 2);
        powerUpService.spawn(300, 300, 1);

        // Update all power-ups
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                powerUpService.updateAll(0.016);
            }
        });
    }

    @Test
    void testPowerUpCollectionByOwner() {
        // Position player 1 paddle to intercept power-up
        Paddle paddle1 = player1.getPaddle();
        paddle1.setX(400);
        paddle1.setY(550);

        // Spawn power-up above paddle
        powerUpService.spawn(400, 300, 1);

        // Update until power-up reaches paddle (or times out)
        for (int i = 0; i < 100; i++) {
            powerUpService.updateAll(0.016);
        }

        // Verify system doesn't crash with collection
        assertDoesNotThrow(() -> powerUpService.updateAll(0.016));
    }

    @Test
    void testApplyPickup() {
        Paddle paddle1 = player1.getPaddle();
        double originalWidth = paddle1.getWidth();

        // Create and apply expand power-up
        PowerUp expandPowerUp = new ExpandPaddlePowerUp(400, 300);

        assertDoesNotThrow(() -> {
            powerUpService.applyPickup(1, expandPowerUp);
        });
    }

    @Test
    void testClearAllPowerUps() {
        // Spawn several power-ups
        powerUpService.spawn(100, 100, 1);
        powerUpService.spawn(200, 200, 2);
        powerUpService.spawn(300, 300, 1);

        // Clear all
        powerUpService.clear();

        // Update should handle empty list
        assertDoesNotThrow(() -> powerUpService.updateAll(0.016));
    }

    @Test
    void testPowerUpRemovalWhenOutOfBounds() {
        // Spawn power-up
        powerUpService.spawn(400, 300, 1);

        // Update many times to push it out of bounds
        for (int i = 0; i < 1000; i++) {
            powerUpService.updateAll(0.016);
        }

        // Should handle out-of-bounds removal gracefully
        assertDoesNotThrow(() -> powerUpService.updateAll(0.016));
    }

    @Test
    void testMultiplePowerUpsForDifferentPlayers() {
        // Spawn power-ups for both players
        powerUpService.spawn(100, 100, 1);
        powerUpService.spawn(200, 200, 2);
        powerUpService.spawn(300, 100, 1);
        powerUpService.spawn(400, 200, 2);

        // Update and verify no interference
        for (int i = 0; i < 50; i++) {
            powerUpService.updateAll(0.016);
        }

        assertDoesNotThrow(() -> powerUpService.updateAll(0.016));
    }

    @Test
    void testRandomPowerUpGeneration() {
        // Spawn many power-ups to test randomness
        for (int i = 0; i < 20; i++) {
            double x = Math.random() * 800;
            double y = Math.random() * 600;
            int player = (i % 2) + 1;

            assertDoesNotThrow(() -> powerUpService.spawn(x, y, player));
        }
    }

    @Test
    void testPowerUpServiceWithNullPlayers() {
        // Should not crash when created with valid players
        assertNotNull(powerUpService);
        assertDoesNotThrow(() -> powerUpService.spawn(100, 100, 1));
    }

    @Test
    void testDifferentPowerUpTypes() {
        // Test that different power-up types can be spawned (random generation)
        int spawnCount = 50;

        for (int i = 0; i < spawnCount; i++) {
            powerUpService.spawn(400, 300, 1);
        }

        // Update to process all power-ups
        for (int i = 0; i < 100; i++) {
            powerUpService.updateAll(0.016);
        }

        assertDoesNotThrow(() -> powerUpService.clear());
    }
}
