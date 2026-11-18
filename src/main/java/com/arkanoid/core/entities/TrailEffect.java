package com.arkanoid.core.entities;

import com.arkanoid.utils.ColorCache;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TrailEffect extends GameObject {
    private double life;
    private double maxLife;
    private double initialRadius;
    private Color color;

    public TrailEffect(double x, double y, double radius, double lifeDuration, Color color) {
        super(x - radius, y - radius, radius * 2, radius * 2);
        this.initialRadius = radius;
        this.life = lifeDuration;
        this.maxLife = lifeDuration;
        this.color = color != null ? color : Color.CYAN;
    }

    /**
     * Reset trail effect for object pool reuse.
     */
    public void reset(double x, double y, double radius, double lifeDuration, Color color) {
        this.x = x - radius;
        this.y = y - radius;
        this.width = radius * 2;
        this.height = radius * 2;
        this.initialRadius = radius;
        this.life = lifeDuration;
        this.maxLife = lifeDuration;
        this.color = color != null ? color : Color.CYAN;
        this.active = lifeDuration > 0;
    }

    @Override
    public void update(double deltaTime) {
        life -= deltaTime;
        if (life <= 0) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (life > 0 && maxLife > 0) {
            double progress = life / maxLife;
            double currentRadius = initialRadius * progress;
            double currentOpacity = progress * 0.7;

            double glowRadius = currentRadius * 1.5;
            double glowOpacity = currentOpacity * 0.3;
            gc.setFill(ColorCache.getWithAlpha(color, glowOpacity));
            gc.fillOval(
                    x + (initialRadius - glowRadius),
                    y + (initialRadius - glowRadius),
                    glowRadius * 2,
                    glowRadius * 2
            );

            gc.setFill(ColorCache.getWithAlpha(color, currentOpacity));
            gc.fillOval(
                    x + (initialRadius - currentRadius),
                    y + (initialRadius - currentRadius),
                    currentRadius * 2,
                    currentRadius * 2
            );

            double centerRadius = currentRadius * 0.5;
            double centerOpacity = Math.min(currentOpacity * 1.5, 1.0);
            gc.setFill(ColorCache.getWithAlpha(Color.WHITE, centerOpacity));
            gc.fillOval(
                    x + (initialRadius - centerRadius),
                    y + (initialRadius - centerRadius),
                    centerRadius * 2,
                    centerRadius * 2
            );
        }
    }

    @Override
    public boolean isActive() {
        return life > 0;
    }
}
