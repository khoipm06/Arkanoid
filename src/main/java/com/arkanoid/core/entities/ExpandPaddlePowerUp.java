package com.arkanoid.core.entities;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ExpandPaddlePowerUp extends PowerUp {
    private static final double EXPAND_AMOUNT = 30;

    public ExpandPaddlePowerUp(double x, double y) {
        super(x, y, 20);
        this.color = Color.GREEN;
        this.image = new Image(getClass().getResource("/images/Up_Size.png").toExternalForm());
    }

    @Override
    public void applyEffect(Paddle paddle) {

        paddle.expand(EXPAND_AMOUNT);
    }

    @Override
    public void removeEffect(Paddle paddle) {
        paddle.resetSize();
    }
}
