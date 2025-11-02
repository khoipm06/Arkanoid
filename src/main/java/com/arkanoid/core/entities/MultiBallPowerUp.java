package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class MultiBallPowerUp extends PowerUp {
    public MultiBallPowerUp(double x, double y) {
        super(x, y, 20);
        this.color = Color.YELLOW;
    }

    @Override
    public void applyEffect(Paddle paddle) {
    }
    @Override
    public void removeEffect(Paddle paddle) {

    }
}
