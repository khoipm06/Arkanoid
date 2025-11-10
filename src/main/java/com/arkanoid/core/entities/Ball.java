package com.arkanoid.core.entities;

import com.arkanoid.systems.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.io.InputStream;

public class Ball extends MovableObject {
    private double radius;
    private Color color;
    private double minX, minY, maxX, maxY;
    private boolean attachedToPaddle = false;
    private Image ballImage;
    private boolean isExplosive = false;


    public Ball(double x, double y, double radius, double speed) {
        super(x - radius, y - radius, radius * 2, radius * 2, speed);
        this.radius = radius;
        this.color = Color.RED;
//        this.velocityX = speed * 0.7;
//        this.velocityY = -speed;
        this.velocityX = 0;
        this.velocityY = 0;
        attachedToPaddle = true;

        loadDefaultImage();
    }



        public void setBounds(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    public void checkPaddleCollision(Paddle paddle) {
        if (intersects(paddle) && velocityY > 0) {
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

    public boolean isExplosive() {
        return isExplosive;
    }

    public void setExplosive(boolean explosive) {
        isExplosive = explosive;
    }
}
