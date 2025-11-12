package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

public class Particle extends GameObject {
    private double velX;
    private double velY;
    private double life;
    private Color color;
    private static final Random rand = new Random();

    public Particle(double x, double y, Color color) {
        super(x, y, rand.nextInt(5) + 8, rand.nextInt(5) + 8); // Particles now randomly sized between 8 and 12
        this.color = color;
        this.velX = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50); // Random velocity
        this.velY = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50);
        this.life = rand.nextDouble() * 0.5 + 0.3; // Lifespan between 0.3 and 0.8 seconds
    }
    
    /**
     * Reset particle for object pool reuse.
     */
    public void reset(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.width = rand.nextInt(5) + 8;
        this.height = rand.nextInt(5) + 8;
        this.color = color;
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
        if (!active) return;
        // Fade out effect
        gc.setGlobalAlpha(Math.max(0, life / (0.5 + 0.3)));
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        gc.setGlobalAlpha(1.0); // Reset alpha
    }
}
