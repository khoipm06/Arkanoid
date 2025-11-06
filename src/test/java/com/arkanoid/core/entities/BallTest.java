package com.arkanoid.core.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BallTest {

    private Ball ball;

    @BeforeEach
    void setUp() {
        ball = new Ball(100, 100, 10, 100);
        ball.setBounds(0, 0, 800, 600);
    }

    @Test
    void testUpdate() {
        ball.launch();
        double initialY = ball.getY();
        ball.update(0.1);
        assertTrue(ball.getY() < initialY);
    }

    @Test
    void testCheckWallCollision() {
        ball.setX(0);
        ball.setVelocityX(-100);
        ball.update(0.1);
        assertTrue(ball.getVelocityX() > 0);

        ball.setX(790);
        ball.setVelocityX(100);
        ball.update(0.1);
        assertTrue(ball.getVelocityX() < 0);

        ball.setY(0);
        ball.setVelocityY(-100);
        ball.update(0.1);
        assertTrue(ball.getVelocityY() > 0);
    }

    @Test
    void testLaunch() {
        assertTrue(ball.isAttachedToPaddle());
        ball.launch();
        assertFalse(ball.isAttachedToPaddle());
        assertTrue(ball.getVelocityY() < 0);
    }
}
