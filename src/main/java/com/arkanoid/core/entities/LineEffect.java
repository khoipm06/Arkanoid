package com.arkanoid.core.entities;

import com.arkanoid.utils.ColorCache;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LineEffect extends GameObject {
    private static final Color LIGHTNING_COLOR = Color.color(0.3, 0.8, 1.0);
    private static final Random random = new Random();

    private final double duration;
    private double age;
    private final double endX;
    private final double endY;
    private final List<double[]> segments;

    public LineEffect(double x, double y, double endX, double endY, double duration) {
        super(x, y, 0, 0);
        this.endX = endX;
        this.endY = endY;
        this.duration = duration;
        this.age = 0;
        this.segments = new ArrayList<>(20);
        generateLightning();
    }

    private void generateLightning() {
        int steps = 20;
        double dx = (endX - x) / steps;
        double dy = (endY - y) / steps;
        double prevX = x;
        double prevY = y;

        for (int i = 1; i <= steps; i++) {
            double nx = x + dx * i + (random.nextDouble() - 0.5) * 8;
            double ny = y + dy * i + (random.nextDouble() - 0.5) * 6;
            segments.add(new double[] { prevX, prevY, nx, ny });
            prevX = nx;
            prevY = ny;
        }
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
        if (!active || duration <= 0) {
            return;
        }
        double alpha = Math.max(0.0, 1.0 - (age / duration));
        gc.setStroke(ColorCache.getWithAlpha(LIGHTNING_COLOR, alpha));
        gc.setLineWidth(2);

        for (double[] seg : segments) {
            gc.strokeLine(seg[0], seg[1], seg[2], seg[3]);
        }
    }
}
