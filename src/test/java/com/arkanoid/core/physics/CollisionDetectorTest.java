package com.arkanoid.core.physics;

import com.arkanoid.core.entities.*;
import com.arkanoid.systems.GameManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CollisionDetectorTest {

    private Ball ball;
    private List<Brick> bricks;
    private GameManager gameManager;

    @BeforeEach
    void setUp() {
        ball = new Ball(100, 100, 10, 100);
        bricks = new ArrayList<>();
        gameManager = new GameManager(800, 600, 1);
    }

    @Test
    void testBallBrickCollision() {
        Brick brick = new NormalBrick(100, 110, 50, 20, 0, 0);
        bricks.add(brick);

        CollisionDetector.checkBallBrickCollisions(ball, bricks, b -> {},
        gameManager);

        assertTrue(brick.isDestroyed());
    }

    @Test
    void testExplosiveBallBrickCollision() {
        ball.setExplosive(true);
        Brick brick1 = new NormalBrick(100, 110, 50, 20, 0, 0);
        Brick brick2 = new NormalBrick(110, 110, 50, 20, 0, 1);
        bricks.add(brick1);
        bricks.add(brick2);

        CollisionDetector.checkBallBrickCollisions(ball, bricks, b -> {},
        gameManager);

        assertTrue(brick1.isDestroyed());
        assertTrue(brick2.isDestroyed());
    }
}
