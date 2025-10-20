package com.arkanoid.core.entities;

import com.arkanoid.systems.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ball extends MovableObject {
    private double radius;
    private Color color;
    private double minX, minY, maxX, maxY;
    private boolean attachedToPaddle = false;


    public Ball(double x, double y, double radius, double speed) {
        super(x - radius, y - radius, radius * 2, radius * 2, speed);
        this.radius = radius;
        this.color = Color.RED;
//        this.velocityX = speed * 0.7;
//        this.velocityY = -speed;
        this.velocityX = 0;
        this.velocityY = 0;
        attachedToPaddle = true;
    }

    public void setBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
        checkWallCollision();
    }

    public void launch() {
        if (attachedToPaddle) {
            attachedToPaddle = false;
            velocityY = -Math.abs(speed); // bay lên trên
            velocityX = 0;                // bay thẳng, hoặc bạn có thể random nhẹ
        }
    }

    private void checkWallCollision() {
        if (x <= minX) {
            x = minX;
            reverseX();
        }
        if (x + width >= maxX) {
            x = maxX - width;
            reverseX();
        }
        if (y <= minY) {
            y = minY;
            reverseY();
        }
    }

    public void checkPaddleCollision(Paddle paddle) {
        if (intersects(paddle) && velocityY > 0) {
            SoundManager.playSound("paddleBounce.wav");
            y = paddle.getY() - height;
            
            double hitPosition = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() / 2);
            velocityX = hitPosition * speed * 0.8;
            velocityY = -Math.abs(velocityY);
            
            double totalSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (totalSpeed != speed) {
                double ratio = speed / totalSpeed;
                velocityX *= ratio;
                velocityY *= ratio;
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x, y, width, height);
    }

    public boolean isOutOfBounds() {
        return y > maxY;
    }

    public void reset(double x, double y) {
        this.x = x - radius;
        this.y = y - radius;
        this.velocityX = speed * 0.7;
        this.velocityY = -speed;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isAttachedToPaddle() {
        return attachedToPaddle;
    }

    public void setAttachedToPaddle(boolean attachedToPaddle) {
        this.attachedToPaddle = attachedToPaddle;
    }
}
