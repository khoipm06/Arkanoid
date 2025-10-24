package com.arkanoid.core.entities;

import javafx.animation.PauseTransition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public abstract class PowerUp extends MovableObject {
    protected double lifetime;
    protected double age;
    protected Color color;
    protected boolean collected;

    public PowerUp(double x, double y, double size) {
        super(x - size / 2, y - size / 2, size, size, 100);
        this.velocityY = 100;
        this.lifetime = 10.0;
        this.age = 0;
        this.collected = false;
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
        age += deltaTime;
        if (age >= lifetime) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x, y, width, height);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(x, y, width, height);
    }

    public abstract void applyEffect(Paddle paddle);
    public abstract void removeEffect(Paddle paddle);

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
        if (collected) {
            active = false;
        }
    }

    public PowerUp checkPaddleCollision(Paddle paddle) {
        if (!collected && intersects(paddle)) {
            setCollected(true);
            active = false; // Deactivate the power-up object itself
            return this;
        }
        return null;
    }
}
