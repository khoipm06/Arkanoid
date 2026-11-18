package com.arkanoid.core.entities;

import com.arkanoid.core.GameConstants;
import com.arkanoid.utils.ImageLoader;
import javafx.scene.paint.Color;

public class ExpandPaddlePowerUp extends PowerUp {
    private static final double EXPAND_AMOUNT = 30;
    private static final String IMAGE_PATH = "/images/Up_Size.png";

    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, 20);
        this.color = Color.GREEN;
        this.image = ImageLoader.loadImage(IMAGE_PATH);
    }

    @Override
    public void applyEffect(Paddle paddle) {
        paddle.activateExpand(EXPAND_AMOUNT, GameConstants.DEFAULT_POWERUP_DURATION_SECONDS);
    }

    @Override
    public void removeEffect(Paddle paddle) {
        paddle.setExpandTimerSeconds(0.0);
        paddle.resetSize();
    }
}
