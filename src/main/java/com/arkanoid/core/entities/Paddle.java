package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Paddle extends MovableObject {
    private double minX;
    private double maxX;
    private Color color;

    public Paddle(double x, double y, double width, double height, double speed, double minX, double maxX) {
        super(x, y, width, height, speed);
        this.minX = minX;
        this.maxX = maxX;
        this.color = Color.BLUE;
    }

    public void moveLeft(double deltaTime) {
        velocityX = -speed;
        move(deltaTime);
        constrainToBounds();
    }

    public void moveRight(double deltaTime) {
        velocityX = speed;
        move(deltaTime);
        constrainToBounds();
    }

    public void stop() {
        velocityX = 0;
    }

    private void constrainToBounds() {
        if (x < minX) x = minX;
        if (x + width > maxX) x = maxX - width;
    }

    @Override
    public void update(double deltaTime) {
        constrainToBounds();
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(x, y, width, height);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void expand(double amount) {
        width += amount;
        x -= amount / 2;
        constrainToBounds();
    }
}
