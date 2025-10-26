package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class RowClearPowerUp extends PowerUp {
    public RowClearPowerUp(double x, double y) {
        super(x, y, 20);
        this.color = Color.BLUE;
    }

    @Override
    public void applyEffect(Paddle paddle) {
        // Effect applied in GameManager
    }

    @Override
    public void removeEffect(Paddle paddle) {
        // Effect removal handled in GameManager if needed
    }
}
