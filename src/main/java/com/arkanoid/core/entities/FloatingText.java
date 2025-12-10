package com.arkanoid.core.entities;

import com.arkanoid.utils.ColorCache;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FloatingText extends GameObject {
    private String text;
    private double life;
    private double maxLife;
    private double speed;
    private Color color;
    private final Font font;

    public FloatingText(String text, double x, double y, double lifeDuration, double speed, Color color) {
        super(x, y, 0, 0); // Width and height are not relevant for text
        this.text = text;
        this.life = lifeDuration;
        this.maxLife = lifeDuration;
        this.speed = speed;
        this.color = color;
        this.font = Font.font("Arial", FontWeight.BOLD, 16);
    }
    
    /**
     * Reset floating text for object pool reuse.
     */
    public void reset(String text, double x, double y, double lifeDuration, double speed, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.life = lifeDuration;
        this.maxLife = lifeDuration;
        this.speed = speed;
        this.color = color;
        this.active = true;
    }

    public void update(double deltaTime) {
        y -= speed * deltaTime;
        life -= deltaTime;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (life > 0) {
            double progress = life / maxLife;
            double opacity = progress;

            gc.setFont(font);
            gc.setFill(ColorCache.getWithAlpha(color, opacity));
            gc.fillText(text, x, y);
        }
    }

    public boolean isActive() {
        return life > 0;
    }
}
