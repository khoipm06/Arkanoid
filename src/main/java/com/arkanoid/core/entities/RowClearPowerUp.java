package com.arkanoid.core.entities;

import com.arkanoid.utils.ImageLoader;
import javafx.scene.paint.Color;

public class RowClearPowerUp extends PowerUp {
    private static final String IMAGE_PATH = "/images/Thunder_powerup.png";

    public RowClearPowerUp(double x, double y) {
        super(x, y, 20);
        this.color = Color.BLUE;
        this.image = ImageLoader.loadImage(IMAGE_PATH);
    }

    @Override
    public void applyEffect(Paddle paddle) {
    }

    @Override
    public void removeEffect(Paddle paddle) {
    }
}
