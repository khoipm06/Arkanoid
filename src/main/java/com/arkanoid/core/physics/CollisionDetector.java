package com.arkanoid.core.physics;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Brick;
import com.arkanoid.core.entities.GameObject;
import com.arkanoid.core.entities.UnbreakableBrick;
import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import org.slf4j.Logger;

import java.util.List;

public class CollisionDetector {
    private static final Logger logger = GameLogger.getLogger(CollisionDetector.class);
    private static final double EXPLOSION_RADIUS = 80.0;

    private CollisionDetector() {
    }

    public static void checkBallBrickCollisions(Ball ball, List<Brick> bricks, CollisionCallback callback,
            GameManager gameManager) {
        for (Brick brick : bricks) {
            if (brick.isDestroyed()) {
                continue;
            }

            if (ball.intersects((GameObject) brick)) {
                logger.debug("Ball-brick collision detected at ({}, {})", brick.getCenterX(), brick.getCenterY());
                SoundManager.getInstance().playSound("brickBounce.wav");
                handleBallBrickCollision(ball, brick);
                brick.hit();
                if (callback != null) {
                    callback.onBrickHit(brick);
                }
                if (ball.isExplosive() && !ball.hasExploded()) {
                    double explosionX = brick.getCenterX();
                    double explosionY = brick.getCenterY();
                    logger.debug("Explosive ball triggered at ({}, {}) with radius {}",
                            explosionX, explosionY, EXPLOSION_RADIUS);

                    if (gameManager != null) {
                        gameManager.addExplosion(explosionX, explosionY, 64, 64, 1);
                    }

                    for (Brick otherBrick : bricks) {
                        if (otherBrick.isDestroyed() || otherBrick instanceof UnbreakableBrick) {
                            continue;
                        }

                        double dx = otherBrick.getCenterX() - explosionX;
                        double dy = otherBrick.getCenterY() - explosionY;
                        if (dx * dx + dy * dy <= EXPLOSION_RADIUS * EXPLOSION_RADIUS) {
                            otherBrick.instantDestroy();
                            if (callback != null) {
                                callback.onBrickHit(otherBrick);
                            }
                        }
                    }

                    ball.setHasExploded(true);
                }
                if (!ball.isExplosive()) {
                    break;
                }
            }
        }
    }

    /**
     * Resolves bounce reflection and positional separation between a ball and a brick.
     */
    public static void handleBallBrickCollision(Ball ball, Brick brick) {
        double ballCenterX = ball.getCenterX();
        double ballCenterY = ball.getCenterY();
        double brickCenterX = brick.getX() + brick.getWidth() / 2;
        double brickCenterY = brick.getY() + brick.getHeight() / 2;

        double deltaX = ballCenterX - brickCenterX;
        double deltaY = ballCenterY - brickCenterY;

        double overlapX = (brick.getWidth() / 2 + ball.getWidth() / 2) - Math.abs(deltaX);
        double overlapY = (brick.getHeight() / 2 + ball.getHeight() / 2) - Math.abs(deltaY);

        if (overlapX < overlapY) {
            ball.reverseX();
            if (deltaX > 0) {
                ball.setX(brick.getX() + brick.getWidth());
            } else {
                ball.setX(brick.getX() - ball.getWidth());
            }
        } else {
            ball.reverseY();
            if (deltaY > 0) {
                ball.setY(brick.getY() + brick.getHeight());
            } else {
                ball.setY(brick.getY() - ball.getHeight());
            }
        }
    }

    public interface CollisionCallback {
        void onBrickHit(Brick brick);
    }
}
