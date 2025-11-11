package com.arkanoid.core.entities;

import javafx.scene.image.Image;

public class RowClearPowerUp extends PowerUp {
    public RowClearPowerUp(double x, double y) {
        super(x, y, 20);
        try {
            this.image = new Image(getClass().getResource("/images/Thunder_powerup.png").toExternalForm());
        } catch (Exception e) {
            System.out.println("Cannot load the power-up image: " + e.getMessage());
            this.color = javafx.scene.paint.Color.BLUE; // Fallback color
        }
    }

    @Override
    public void applyEffect(Paddle paddle) {

    }

    @Override
    public void removeEffect(Paddle paddle) {

    }
}
