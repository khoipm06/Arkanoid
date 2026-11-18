package com.arkanoid.core.entities;

import com.arkanoid.utils.ColorCache;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FloatingText extends GameObject {
    private static final Font DEFAULT_FONT = Font.font("Arial", FontWeight.BOLD, 16);

    private String text;
    private double life;
    private double maxLife;
    private double speed;
    private Color color;

    public FloatingText(String text, double x, double y, double lifeDuration, double speed, Color color) {
        super(x, y, 0, 0);
        this.text = text;
        this.life = lifeDuration;
        this.maxLife = lifeDuration;
        this.speed = speed;
        this.color = color != null ? color : Color.WHITE;
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
        this.color = color != null ? color : Color.WHITE;
        this.active = lifeDuration > 0;
    }

    @Override
    public void update(double deltaTime) {
        y -= speed * deltaTime;
        life -= deltaTime;
        if (life <= 0) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (life > 0 && maxLife > 0) {
            double opacity = life / maxLife;
            gc.setFont(DEFAULT_FONT);
            gc.setFill(ColorCache.getWithAlpha(color, opacity));
            gc.fillText(text, x, y);
        }
    }

    @Override
    public boolean isActive() {
        return life > 0;
    }
}
