package com.arkanoid.core.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaddleTest {

    private Paddle paddle;

    @BeforeEach
    void setUp() {
        paddle = new Paddle(400, 550, 100, 20, 400, 0, 800);
    }

    @Test
    void testMoveLeft() {
        double initialX = paddle.getX();
        paddle.moveLeft(0.1);
        assertTrue(paddle.getX() < initialX);
    }

    @Test
    void testMoveRight() {
        double initialX = paddle.getX();
        paddle.moveRight(0.1);
        assertTrue(paddle.getX() > initialX);
    }

    @Test
    void testPaddleBounds() {
        paddle.setX(0);
        paddle.moveLeft(0.1);
        assertEquals(0, paddle.getX());

        paddle.setX(700);
        paddle.moveRight(0.1);
        assertEquals(700, paddle.getX());
    }

    @Test
    void testExpandPowerUpExpiresByGameTime() {
        ExpandPaddlePowerUp powerUp = new ExpandPaddlePowerUp(450, 550);
        powerUp.applyEffect(paddle);

        assertEquals(130.0, paddle.getWidth(), 0.001);

        paddle.update(1.0);
        assertEquals(130.0, paddle.getWidth(), 0.001);

        // Collecting a second expand refreshes duration without stacking width
        powerUp.applyEffect(paddle);
        assertEquals(130.0, paddle.getWidth(), 0.001);

        paddle.update(1.5);
        assertEquals(130.0, paddle.getWidth(), 0.001);

        paddle.update(0.6);
        assertEquals(100.0, paddle.getWidth(), 0.001);
    }

    @Test
    void testGunPowerUpExpiresByGameTime() {
        GunPaddlePowerUp powerUp = new GunPaddlePowerUp(450, 550);
        powerUp.applyEffect(paddle);

        assertTrue(paddle.isGunMode());

        paddle.update(1.0);
        assertTrue(paddle.isGunMode());

        paddle.update(1.1);
        assertFalse(paddle.isGunMode());
    }

    @Test
    void testInstanceSkinDoesNotClobberOtherPaddle() {
        Paddle player2Paddle = new Paddle(400, 50, 100, 20, 400, 0, 800);

        paddle.equipSkin("paddle_Metal");
        player2Paddle.equipSkin("paddle_Neon");

        paddle.update(0.016);
        player2Paddle.update(0.016);

        assertEquals("paddle_Metal", paddle.getCurrentSkin());
        assertEquals("paddle_Neon", player2Paddle.getCurrentSkin());
    }
}
