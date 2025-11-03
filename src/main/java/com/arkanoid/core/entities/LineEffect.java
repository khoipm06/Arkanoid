package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LineEffect extends GameObject {
    private double duration;
    private double age;
    private double endX;
    private double endY;

    public LineEffect(double x, double y, double endX, double endY, double duration) {
        super(x, y, 0, 0);
        this.endX = endX;
        this.endY = endY;
        this.duration = duration;
        this.age = 0;
    }

    @Override
    public void update(double deltaTime) {
        age += deltaTime;
        if (age >= duration) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        double alpha = 1.0 - (age / duration);
        gc.setStroke(new Color(0.5, 0.5, 1, alpha));
        gc.setLineWidth(10);
        gc.strokeLine(x, y, endX, endY);
    }
}
