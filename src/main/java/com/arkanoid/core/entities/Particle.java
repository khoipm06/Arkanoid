package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

public class Particle extends GameObject {
    private double velX;
    private double velY;
    private double life;
    private final Color color;
    private static final Random rand = new Random();

    public Particle(double x, double y, Color color) {
        super(x, y, rand.nextInt(3) + 2, rand.nextInt(3) + 2);
        this.color = color;
        this.velX = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50); // Random velocity
        this.velY = (rand.nextDouble() - 0.5) * (rand.nextInt(150) + 50);
        this.life = rand.nextDouble() * 0.5 + 0.3; // Lifespan between 0.3 and 0.8 seconds
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
