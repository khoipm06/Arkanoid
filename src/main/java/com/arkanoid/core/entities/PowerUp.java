package com.arkanoid.core.entities;

import com.arkanoid.systems.logging.GameLogger;
import javafx.animation.PauseTransition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public abstract class PowerUp extends MovableObject {
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
            GameLogger.debug("PowerUp collected: {} at ({}, {})", this.getClass().getSimpleName(), x, y);
            setCollected(true);
            applyEffect(paddle);
            GameLogger.debug("PowerUp effect applied: {}", this.getClass().getSimpleName());
            PauseTransition pause = new PauseTransition(Duration.seconds(2));

            // Khi hết 2 giây, tắt hiệu ứng
            pause.setOnFinished(e -> {
                GameLogger.debug("PowerUp effect removed: {}", this.getClass().getSimpleName());
                removeEffect(paddle);
                setCollected(false);
            });

            // Bắt đầu đếm ngược
            pause.play();
            return true;
        }
        return false;
    }
}
