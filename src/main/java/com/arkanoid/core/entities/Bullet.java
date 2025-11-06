package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet extends GameObject {
    private double speedY; // negative = lên trên
    private Color color;

    public Bullet(double x, double y, double width, double height, double speedY) {
        super(x, y, width, height);
        this.speedY = speedY;
        this.color = Color.YELLOW;
        this.active = true;
    }

    public void update(double deltaTime) {
        y += speedY * deltaTime;
        // nếu ra khỏi màn hình thì deactivate (GameManager sẽ remove)
    }

    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
    }

    public boolean isOutOfBounds(double gameHeight) {
        return y + height < 0 || y > gameHeight;
    }
}
