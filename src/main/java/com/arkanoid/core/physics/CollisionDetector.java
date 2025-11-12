package com.arkanoid.core.physics;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Brick;
import com.arkanoid.core.entities.GameObject;
import com.arkanoid.core.entities.UnbreakableBrick;
import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;

import java.util.ArrayList;
import java.util.List;

public class CollisionDetector {

    public static void checkBallBrickCollisions(Ball ball, List<Brick> bricks, CollisionCallback callback,
            GameManager gameManager) {
        List<Brick> bricksToExplode = new ArrayList<>();
        for (Brick brick : bricks) {
            if (brick.isDestroyed())
                continue;

            if (ball.intersects((GameObject) brick)) {
                GameLogger.debug("Ball-brick collision detected at ({}, {})", brick.getCenterX(), brick.getCenterY());
                SoundManager.getInstance().playSound("brickBounce.wav");
                handleBallBrickCollision(ball, brick);
                brick.hit();
                if (callback != null) {
                    callback.onBrickHit(brick);
                }
                if (ball.isExplosive() && !ball.hasExploded()) {
                    double explosionRadius = 80;
                    double explosionX = brick.getCenterX();
                    double explosionY = brick.getCenterY();
                    GameLogger.debug("Explosive ball triggered at ({}, {}) with radius {}", explosionX, explosionY, explosionRadius);

                    gameManager.addExplosion(explosionX, explosionY, 64, 64, 1);

                    for (Brick otherBrick : bricks) {
                        if (otherBrick.isDestroyed())
                            continue;

                        // Bỏ qua gạch không phá được
                        if (otherBrick instanceof UnbreakableBrick)
                            continue;

                        double dx = otherBrick.getCenterX() - explosionX;
                        double dy = otherBrick.getCenterY() - explosionY;
                        double dist = Math.sqrt(dx * dx + dy * dy);

                        if (dist <= explosionRadius) {
                            if (otherBrick instanceof UnbreakableBrick)
                                continue;

                            otherBrick.instantDestroy(); // Phá ngay, bỏ qua hitCount
                            if (callback != null)
                                callback.onBrickHit(otherBrick);
                        }
                    }

                    ball.setHasExploded(true);
                }
                // Only one brick can be hit at a time by a non-explosive ball
                if (!ball.isExplosive()) {
                    break;
                }
            }
        }
        for (Brick explodedBrick : bricksToExplode) {
            double explosionCenterX = explodedBrick.getCenterX();
            double explosionCenterY = explodedBrick.getCenterY();
            double explosionRadius = 50; // Define explosion radius

            for (Brick otherBrick : bricks) {
                if (otherBrick.isDestroyed())
                    continue;

                double distance = Math.sqrt(
                        Math.pow(otherBrick.getCenterX() - explosionCenterX, 2) +
                                Math.pow(otherBrick.getCenterY() - explosionCenterY, 2));

                if (distance <= explosionRadius) {
                    otherBrick.destroy();
                    if (callback != null) {
                        callback.onBrickHit(otherBrick);
                    }
                }
            }
        }
    }

    private static void handleBallBrickCollision(Ball ball, Brick brick) {
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
