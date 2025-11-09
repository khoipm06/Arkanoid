package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Explosion extends GameObject {
    private double maxRadius;
    private double currentRadius;
    private final double duration;
    private double age;
    private final int frameCount = 6;     // số frame trong sprite sheet
    private int currentFrame = 0;
    private final double frameWidth;
    private final double frameHeight;
    private Image image;

    public Explosion(double x, double y, double frameWidth, double frameHeight, double duration) {
        super(x, y, 0, 0);
        this.maxRadius = maxRadius;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.duration = duration;
        this.age = 0;
        var url = getClass().getResource("/images/Explosion.png");
        if (url != null) {
            this.image = new Image(url.toExternalForm());
        } else {
            System.out.println("Explosion image not found!");
        }
    }

    @Override
    public void update(double deltaTime) {
        age += deltaTime;
        double progress = Math.min(age / duration, 1.0);
        currentFrame = Math.min((int) (progress * frameCount), frameCount - 1);

        // Khi hết thời gian vụ nổ thì tắt
        if (age >= duration) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image == null) return;

        // Vẽ frame hiện tại
        gc.drawImage(
                image,
                currentFrame * frameWidth, 0, frameWidth, frameHeight,   // cắt frame từ sprite sheet
                x - frameWidth / 2, y - frameHeight / 2, frameWidth, frameHeight // vẽ tại tâm (x,y)
        );
    }
}
