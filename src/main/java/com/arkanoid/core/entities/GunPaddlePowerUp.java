package com.arkanoid.core.entities;

import com.arkanoid.core.GameConstants;
import com.arkanoid.utils.ImageLoader;
import javafx.scene.paint.Color;

public class GunPaddlePowerUp extends PowerUp {
    private static final String IMAGE_PATH = "/images/GunPowerUp.png";

    public GunPaddlePowerUp(double x, double y) {
        super(x, y, 18);
        this.color = Color.ORANGE;
        this.lifetime = 10.0;
        this.age = 0;
        this.active = true;
        this.image = ImageLoader.loadImage(IMAGE_PATH);
    }

    @Override
    public void applyEffect(Paddle paddle) {
        paddle.activateGun(GameConstants.DEFAULT_POWERUP_DURATION_SECONDS);
    }

    @Override
    public void removeEffect(Paddle paddle) {
        paddle.setGunMode(false);
    }
}
