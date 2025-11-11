package com.arkanoid.core.entities;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class GunPaddlePowerUp extends PowerUp {
    private final long expiryNano;
    private long appliedExpiryNano = -1;

    public GunPaddlePowerUp(double x, double y) {
        super(x, y, 18);
        this.color = Color.ORANGE;
        this.lifetime = 10.0;
        this.age = 0;
        this.active = true;
        var url = getClass().getResource("/images/GunPowerUp.png");
        if (url != null) {
            this.image = new Image(url.toExternalForm());
        } else {
            System.out.println("GunPaddlePowerUp image not found!");
        }
        this.expiryNano = 0;
    }

    @Override
    public void applyEffect(Paddle paddle) {
        // compute expiry time 2s from now
        long expiry = System.nanoTime() + (long)(2.0 * 1_000_000_000L);
        appliedExpiryNano = expiry;
        paddle.setGunExpiry(expiry);   // store expiry into paddle
        paddle.setGunMode(true);
    }

    @Override
    public void removeEffect(Paddle paddle) {
        // Only remove if this instance is responsible for the current expiry
        long currentExpiry = paddle.getGunExpiry();
        if (appliedExpiryNano != -1 && currentExpiry == appliedExpiryNano) {
            paddle.setGunMode(false);
            paddle.setGunExpiry(-1);
        }
    }
}
