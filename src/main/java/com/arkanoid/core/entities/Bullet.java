package com.arkanoid.core.entities;

import com.arkanoid.utils.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Bullet extends GameObject {
    private static final String BULLET_IMAGE_PATH = "/images/bullet.png";
    private double speedY;
    private final Image bulletImage;

    public Bullet(double x, double y, double width, double height, double speedY) {
        super(x, y, width, height);
        this.speedY = speedY;
        this.active = true;
        this.bulletImage = ImageLoader.loadImage(BULLET_IMAGE_PATH);
    }

    /**
     * Reset bullet for object pool reuse.
     */
    public void reset(double x, double y, double width, double height, double speedY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speedY = speedY;
        this.active = true;
    }

    @Override
    public void update(double deltaTime) {
        y += speedY * deltaTime;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (bulletImage != null) {
            gc.drawImage(bulletImage, x, y, width, height);
        } else {
            gc.setFill(javafx.scene.paint.Color.YELLOW);
            gc.fillRect(x, y, width, height);
        }
    }

    public boolean isOutOfBounds(double gameHeight) {
        return y + height < 0 || y > gameHeight;
    }
}
