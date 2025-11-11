package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TrailEffect extends GameObject {
    private double life;
    private final double maxLife;
    private final double initialRadius;
    private final Color color;

    public TrailEffect(double x, double y, double radius, double lifeDuration, Color color) {
        super(x - radius, y - radius, radius * 2, radius * 2);
        this.initialRadius = radius;
        this.life = lifeDuration;
        this.maxLife = lifeDuration;
        this.color = color;
    }

    public void update(double deltaTime) {
        life -= deltaTime;
        // Optionally, make the trail move slightly or drift
        // For now, it just fades and shrinks in place
    }

    @Override
    public void render(GraphicsContext gc) {
        if (life > 0) {
            double progress = life / maxLife; // 1.0 at start, 0.0 at end
            double currentRadius = initialRadius * progress;
            double currentOpacity = progress;

            gc.setFill(new Color(color.getRed(), color.getGreen(), color.getBlue(), currentOpacity));
            gc.fillOval(x + (initialRadius - currentRadius), y + (initialRadius - currentRadius), currentRadius * 2, currentRadius * 2);
        }
    }

    public boolean isActive() {
        return life > 0;
    }
}
