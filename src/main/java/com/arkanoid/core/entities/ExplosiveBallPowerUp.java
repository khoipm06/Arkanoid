package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class ExplosiveBallPowerUp extends PowerUp {
    public ExplosiveBallPowerUp(double x, double y) {
        super(x, y, 20);
        this.color = Color.RED;
    }

    @Override
    public void applyEffect(Paddle paddle) {
    }
    @Override
    public void removeEffect(Paddle paddle) {

    }
}
