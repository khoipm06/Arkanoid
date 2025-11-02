package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class MovingBrick extends BaseBrick {
    private double velocityX;
    private double minX;
    private double maxX;
    private double moveSpeed;

    public MovingBrick(double x, double y, double width, double height, double minX, double maxX) {
        super(x, y, width, height, 2, Color.CYAN);
        this.minX = minX;
        this.maxX = maxX;
        this.moveSpeed = 100;
        this.velocityX = moveSpeed;
    }

    @Override
    public void update(double deltaTime) {
        x += velocityX * deltaTime;
        
        if (x <= minX) {
            x = minX;
            velocityX = moveSpeed;
        } else if (x + width >= maxX) {
            x = maxX - width;
            velocityX = -moveSpeed;
        }
    }
}
