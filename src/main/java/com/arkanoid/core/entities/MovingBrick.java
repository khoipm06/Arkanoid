package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class MovingBrick extends BaseBrick {
    private double velocityX;
    private double minX;
    private double maxX;
    private double moveSpeed;

    public MovingBrick(double x, double y, double width, double height, double minX, double maxX, int row, int col, String path) {
        super(x, y, width, height, 1, Color.CYAN, row, col, path);
        this.minX = minX;
        this.maxX = maxX;
        this.moveSpeed = 50;
        this.velocityX = moveSpeed;
    }

    @Override
    public void update(double deltaTime) {
        double newX = x + velocityX * deltaTime;

        if (newX < minX) {
            newX = minX;
            velocityX = moveSpeed;           // Đi sang phải
        } else if (newX + width > maxX) {
            newX = maxX - width;
            velocityX = -moveSpeed;          // Đi sang trái
        }

        x = newX;
    }
}
