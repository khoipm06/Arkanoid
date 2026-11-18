package com.arkanoid.core.entities;

import com.arkanoid.utils.ColorCache;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

public class Particle extends GameObject {
    private static final double MAX_LIFE = 0.8;
    private static final Random rand = new Random();

    private double velX;
    private double velY;
    private double life;
    private Color color;

    public Particle(double x, double y, Color color) {
        super(x, y, rand.nextInt(5) + 8, rand.nextInt(5) + 8);
        this.color = color != null ? color : Color.WHITE;
        this.velX = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50);
        this.velY = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50);
        this.life = rand.nextDouble() * 0.5 + 0.3;
    }

    /**
     * Reset particle for object pool reuse.
     */
    public void reset(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.width = rand.nextInt(5) + 8;
        this.height = rand.nextInt(5) + 8;
        this.color = color != null ? color : Color.WHITE;
        this.velX = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50);
        this.velY = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50);
        this.life = rand.nextDouble() * 0.5 + 0.3;
        this.active = true;
    }

    @Override
    public void update(double deltaTime) {
        x += velX * deltaTime;
        y += velY * deltaTime;
        life -= deltaTime;
        if (life <= 0) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!active || life <= 0) {
            return;
        }
        double alpha = Math.max(0.0, Math.min(1.0, life / MAX_LIFE));
        gc.setFill(ColorCache.getWithAlpha(color, alpha));
        gc.fillRect(x, y, width, height);
    }
}
