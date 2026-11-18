package com.arkanoid.systems;

import com.arkanoid.core.GameConstants;
import com.arkanoid.core.entities.*;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        gameManager = GameManager.getInstance(800, 600, 1);
    }

    @AfterEach
    void tearDown() {
        if (gameManager != null) {
            gameManager.cleanup();
        }
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

        ball.setExplosive(true);
        assertTrue(ball.isExplosive());
    }

    @Test
    void testGameOver() {
        gameManager.startGame();
        gameManager.getPlayer().getState().setLives(1);

        if (!gameManager.getBalls().isEmpty()) {
            Ball ball = gameManager.getBalls().get(0);
            ball.launch();
            ball.setBounds(0, 0, 800, 600);
            ball.setY(650);
            ball.setVelocityY(10);
            gameManager.update(0.16);
        }

        assertEquals(GameManager.GameState.GAME_OVER, gameManager.getCurrentState());
    }

    @Test
    void testUpdateClampsLargeDeltaAndPausesPowerUps() {
        gameManager.startGame();
        gameManager.update(5.0);
        assertEquals(GameConstants.MAX_FRAME_DELTA, gameManager.getElapsedTimeSeconds(), 1e-6);

        Paddle paddle = gameManager.getPlayer().getPaddle();
        paddle.activateGun(GameConstants.DEFAULT_POWERUP_DURATION_SECONDS);
        assertTrue(paddle.isGunMode());

        gameManager.pause();
        gameManager.update(5.0);

        assertEquals(GameConstants.MAX_FRAME_DELTA, gameManager.getElapsedTimeSeconds(), 1e-6);
        assertTrue(paddle.isGunMode(), "Power-up timer must pause while game is PAUSED");
    }

    @Test
    void testLevelCompletesWhenOnlyUnbreakableBricksRemain() {
        gameManager.startGame();
        List<Brick> bricks = gameManager.getBricks();
        bricks.clear();

        NormalBrick breakable = new NormalBrick(100, 100, 50, 20, 0, 0, null);
        UnbreakableBrick unbreakable = new UnbreakableBrick(200, 100, 50, 20, 0, 1, null);
        bricks.add(breakable);
        bricks.add(unbreakable);

        gameManager.update(0.016);
        assertEquals(GameManager.GameState.PLAYING, gameManager.getCurrentState());

        breakable.instantDestroy();
        gameManager.update(0.016);
        assertEquals(GameManager.GameState.LEVEL_COMPLETE, gameManager.getCurrentState());
    }

    @Test
    void testCleanupReleasesPoolsAndClearsCollections() {
        gameManager.startGame();
        Paddle paddle = gameManager.getPlayer().getPaddle();
        paddle.activateGun(2.0);
        gameManager.update(0.25);

        Brick brick = new NormalBrick(100, 100, 50, 20, 0, 0, null);
        brick.instantDestroy();
        gameManager.onBrickDestroyed(brick);
        gameManager.addExplosion(100, 100, 64, 64, 1.0);
        gameManager.getVisualEffectsManager().spawnLineEffect(0, 100, 800, 100, 0.5);
        gameManager.getVisualEffectsManager().spawnFloatingText("test", 100, 100, 1.0, 50, Color.WHITE);

        assertFalse(gameManager.getBalls().isEmpty());
        assertFalse(gameManager.getParticles().isEmpty());

        gameManager.cleanup();

        assertTrue(gameManager.getBalls().isEmpty());
        assertTrue(gameManager.getBricks().isEmpty());
        assertTrue(gameManager.getBullets().isEmpty());
        assertTrue(gameManager.getPowerUps().isEmpty());
        assertTrue(gameManager.getParticles().isEmpty());
        assertTrue(gameManager.getTrailEffects().isEmpty());
        assertTrue(gameManager.getFloatingTexts().isEmpty());
        assertTrue(gameManager.getExplosions().isEmpty());
        assertTrue(gameManager.getLineEffects().isEmpty());
        assertNull(PlayerManager.getInstance().getPlayer(1));
    }

    @Test
    void testMultiBallInheritsSourceBallInstanceSkin() {
        gameManager.startGame();
        Ball primary = gameManager.getBalls().get(0);
        primary.equipSkin("Fire");
        primary.launch();

        Paddle paddle = gameManager.getPlayer().getPaddle();
        MultiBallPowerUp multiBall = new MultiBallPowerUp(paddle.getX() + 10, paddle.getY());
        gameManager.getPowerUps().add(multiBall);

        gameManager.update(0.016);

        assertEquals(3, gameManager.getBalls().size());
        for (Ball b : gameManager.getBalls()) {
            assertEquals("Fire", b.getEquippedSkin());
        }
    }
}
