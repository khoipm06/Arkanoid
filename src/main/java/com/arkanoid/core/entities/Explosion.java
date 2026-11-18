package com.arkanoid.core.entities;

import com.arkanoid.utils.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Explosion extends GameObject {
    private static final String EXPLOSION_IMAGE_PATH = "/images/Explosion.png";
    private final double duration;
    private double age;
    private final int frameCount = 6;
    private int currentFrame = 0;
    private final double frameWidth;
    private final double frameHeight;
    private final Image image;

    public Explosion(double x, double y, double frameWidth, double frameHeight, double duration) {
        super(x, y, 0, 0);
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.duration = duration;
        this.age = 0;
        this.image = ImageLoader.loadImage(EXPLOSION_IMAGE_PATH);
    }

    @Override
    public void update(double deltaTime) {
        age += deltaTime;
        double progress = Math.min(age / duration, 1.0);
        currentFrame = Math.min((int) (progress * frameCount), frameCount - 1);

        if (age >= duration) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image == null)
            return;

        gc.drawImage(
                image,
                currentFrame * frameWidth, 0, frameWidth, frameHeight,
                x - frameWidth / 2, y - frameHeight / 2, frameWidth, frameHeight
        );
    }
}
