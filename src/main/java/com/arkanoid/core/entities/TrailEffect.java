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
        this.color = color;
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
        this.color = color;
        this.active = true;
    }

    public void update(double deltaTime) {
        life -= deltaTime;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (life > 0) {
            double progress = life / maxLife;
            double currentRadius = initialRadius * progress;
            // Slightly more transparent for a smoother look
            double currentOpacity = progress * 0.7;

            // Draw outer glow (larger, more transparent)
            double glowRadius = currentRadius * 1.5;
            double glowOpacity = currentOpacity * 0.3;
            gc.setFill(ColorCache.getWithAlpha(color, glowOpacity));
            gc.fillOval(
                x + (initialRadius - glowRadius), 
                y + (initialRadius - glowRadius), 
                glowRadius * 2, 
                glowRadius * 2
            );

            // Draw the main trail circle
            gc.setFill(ColorCache.getWithAlpha(color, currentOpacity));
            gc.fillOval(
                x + (initialRadius - currentRadius), 
                y + (initialRadius - currentRadius), 
                currentRadius * 2, 
                currentRadius * 2
            );
            
            // Draw bright center (smaller, more opaque)
            double centerRadius = currentRadius * 0.5;
            double centerOpacity = Math.min(currentOpacity * 1.5, 1.0);
            gc.setFill(ColorCache.getColor(1.0, 1.0, 1.0, centerOpacity)); // White center
            gc.fillOval(
                x + (initialRadius - centerRadius), 
                y + (initialRadius - centerRadius), 
                centerRadius * 2, 
                centerRadius * 2
            );
        }
    }

    public boolean isActive() {
        return life > 0;
    }
}
