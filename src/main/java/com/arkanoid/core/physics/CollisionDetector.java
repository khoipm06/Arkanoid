package com.arkanoid.core.physics;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Brick;
import com.arkanoid.core.entities.Paddle;

import java.util.List;

public class CollisionDetector {
    
    public static void checkBallBrickCollisions(Ball ball, List<Brick> bricks, CollisionCallback callback) {
        for (Brick brick : bricks) {
            if (brick.isDestroyed()) continue;
            
            if (ball.intersects((com.arkanoid.core.entities.GameObject) brick)) {
                handleBallBrickCollision(ball, brick);
                brick.hit();
                if (callback != null) {
                    callback.onBrickHit(brick);
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
