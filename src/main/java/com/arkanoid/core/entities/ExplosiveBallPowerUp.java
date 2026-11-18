package com.arkanoid.core.entities;

import com.arkanoid.utils.ImageLoader;
import javafx.scene.paint.Color;

public class ExplosiveBallPowerUp extends PowerUp {
    private static final String IMAGE_PATH = "/images/Explosive.png";

    public ExplosiveBallPowerUp(double x, double y) {
        super(x, y, 20);
        this.color = Color.RED;
        this.image = ImageLoader.loadImage(IMAGE_PATH);
    }

    @Override
    public void applyEffect(Paddle paddle) {
    }

    @Override
    public void removeEffect(Paddle paddle) {
    }
}
