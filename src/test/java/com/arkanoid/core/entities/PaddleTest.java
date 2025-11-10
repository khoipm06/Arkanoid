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
}
