package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Bullet extends GameObject {
    private double speedY; // negative = lên trên
    private Image bulletImage;

    public Bullet(double x, double y, double width, double height, double speedY) {
        super(x, y, width, height);
        this.speedY = speedY;
        this.active = true;
        try {
            bulletImage = new Image(getClass().getResourceAsStream("/images/bullet.png"));
        } catch (Exception e) {
            System.err.println("Không tải được ảnh bullet: " + e.getMessage());
            bulletImage = null;
        }
    }

    public void update(double deltaTime) {
        y += speedY * deltaTime;
        // nếu ra khỏi màn hình thì deactivate (GameManager sẽ remove)
    }

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
