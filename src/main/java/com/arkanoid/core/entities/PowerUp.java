package com.arkanoid.core.entities;

import com.arkanoid.systems.logging.GameLogger;
import javafx.animation.PauseTransition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.slf4j.Logger;

public abstract class PowerUp extends MovableObject {
    protected static final Logger logger = GameLogger.getLogger(PowerUp.class);
    protected double lifetime;
    protected double age;
    protected Color color;
    protected boolean collected;
    protected Image image;

    public PowerUp(double x, double y, double size) {
        super(x - size / 2, y - size / 2, size, size, 100);
        this.velocityY = 100;
        this.lifetime = 10.0;
        this.age = 0;
        this.collected = false;
        this.image = null;
        // Removed TRACE logging to reduce verbosity
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
        age += deltaTime;
        if (age >= lifetime) {
            // Removed TRACE logging to reduce verbosity
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image != null) {
            gc.drawImage(image, x, y, width, height);
        } else {
            gc.setFill(color != null ? color : Color.WHITE);
            gc.fillOval(x, y, width, height);
        }
    }

    public abstract void applyEffect(Paddle paddle);
    public abstract void removeEffect(Paddle paddle);

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
        if (collected) {
            active = false;
        }
    }

    public boolean checkPaddleCollision(Paddle paddle) {
        if (!collected && intersects(paddle)) {
            logger.debug("PowerUp collected: {} at ({}, {})", this.getClass().getSimpleName(), x, y);
            setCollected(true);
            applyEffect(paddle);
            logger.debug("PowerUp effect applied: {}", this.getClass().getSimpleName());
            PauseTransition pause = new PauseTransition(Duration.seconds(2));

            // After 2 seconds, remove the effect
            pause.setOnFinished(e -> {
                logger.debug("PowerUp effect removed: {}", this.getClass().getSimpleName());
                removeEffect(paddle);
                setCollected(false);
            });

            // Start countdown
            pause.play();
            return true;
        }
        return false;
    }
}
