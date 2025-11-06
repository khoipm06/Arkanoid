package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Explosion extends GameObject {
    private double maxRadius;
    private double currentRadius;
    private double duration;
    private double age;

    public Explosion(double x, double y, double maxRadius, double duration) {
        super(x, y, 0, 0);
        this.maxRadius = maxRadius;
        this.duration = duration;
        this.currentRadius = 0;
        this.age = 0;
    }

    @Override
    public void update(double deltaTime) {
        age += deltaTime;
        currentRadius = (age / duration) * maxRadius;
        if (age >= duration) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double alpha = 1.0 - (age / duration);
        gc.setFill(new Color(1, 0.5, 0, alpha));
        gc.fillOval(x - currentRadius, y - currentRadius, currentRadius * 2, currentRadius * 2);
    }
}
