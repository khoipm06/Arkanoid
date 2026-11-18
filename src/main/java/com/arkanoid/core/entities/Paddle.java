package com.arkanoid.core.entities;

import com.arkanoid.utils.ColorCache;
import com.arkanoid.utils.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Set;

public class Paddle extends MovableObject {
    private static final String DEFAULT_SKIN = "paddle_Default";
    private static final Set<String> VALID_SKINS = Set.of(
            "paddle_Default", "paddle_Wood", "paddle_Metal", "paddle_Neon"
    );
    private static final Color HIT_FLASH_OUTER_COLOR = Color.color(0.2, 0.9, 1.0);
    private static final Color HIT_FLASH_INNER_COLOR = Color.WHITE;

    private final double originalWidth;
    private double minX;
    private double maxX;
    private Color color = Color.WHITE;
    private Image paddleImage;
    private String currentSkin;
    private boolean gunMode = false;
    private double gunTimerSeconds = 0.0;
    private double expandTimerSeconds = 0.0;
    private Image gunImage;
    private double collisionX;
    private double collisionY;
    private double hitFlashTime = 0.0;

    public Paddle(double x, double y, double width, double height) {
        this(x, y, width, height, 0, 0, Double.MAX_VALUE);
    }

    public Paddle(double x, double y, double width, double height, double speed, double minX, double maxX) {
        super(x, y, width, height, speed);
        this.minX = minX;
        this.maxX = maxX;
        this.originalWidth = width;
        this.currentSkin = DEFAULT_SKIN;
        this.paddleImage = getSkin(this.currentSkin);
        this.gunImage = ImageLoader.loadImage("/images/gun.png");
    }

    public static Image getSkin(String skinName) {
        String resolved = (skinName != null && VALID_SKINS.contains(skinName)) ? skinName : DEFAULT_SKIN;
        Image img = ImageLoader.loadImage("/images/" + resolved + ".png");
        if (img == null && !DEFAULT_SKIN.equals(resolved)) {
            img = ImageLoader.loadImage("/images/" + DEFAULT_SKIN + ".png");
        }
        return img;
    }

    public String getCurrentSkin() {
        return currentSkin;
    }

    public void moveLeft(double deltaTime) {
        velocityX = -speed;
        move(deltaTime);
        constrainToBounds();
    }

    public void moveRight(double deltaTime) {
        velocityX = speed;
        move(deltaTime);
        constrainToBounds();
    }

    public void stop() {
        velocityX = 0;
    }

    private void constrainToBounds() {
        if (x < minX) {
            x = minX;
        }
        if (x + width > maxX) {
            x = maxX - width;
        }
    }

    @Override
    public void update(double deltaTime) {
        constrainToBounds();
        if (expandTimerSeconds > 0.0) {
            expandTimerSeconds -= deltaTime;
            if (expandTimerSeconds <= 0.0) {
                expandTimerSeconds = 0.0;
                resetSize();
            }
        }
        if (gunMode && gunTimerSeconds > 0.0) {
            gunTimerSeconds -= deltaTime;
            if (gunTimerSeconds <= 0.0) {
                gunTimerSeconds = 0.0;
                gunMode = false;
            }
        }
        if (hitFlashTime > 0.0) {
            hitFlashTime = Math.max(0.0, hitFlashTime - deltaTime);
        }
    }

    public void setPaddleImage(Image image) {
        this.paddleImage = image;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (paddleImage != null) {
            gc.drawImage(paddleImage, x, y, width, height);
        } else {
            gc.setFill(color);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.WHITE);
            gc.strokeRect(x, y, width, height);
        }
        if (isGunMode() && gunImage != null) {
            double gunW = 12, gunH = 24;
            gc.drawImage(gunImage, getLeftGunX(), getGunY(), gunW, gunH);
            gc.drawImage(gunImage, getRightGunX(), getGunY(), gunW, gunH);
        }

        if (hitFlashTime > 0.0) {
            double progress = Math.min(1.0, hitFlashTime / 0.4);
            double intensity = Math.pow(progress, 0.7);
            double currentRadius = width * (1.0 - progress);
            if (currentRadius > 0.0) {
                gc.setFill(ColorCache.getWithAlpha(HIT_FLASH_OUTER_COLOR, intensity * 0.35));
                gc.fillOval(collisionX - currentRadius, collisionY - currentRadius,
                        currentRadius * 2, currentRadius * 2);

                double innerRadius = currentRadius * 0.5;
                gc.setFill(ColorCache.getWithAlpha(HIT_FLASH_INNER_COLOR, intensity * 0.65));
                gc.fillOval(collisionX - innerRadius, collisionY - innerRadius,
                        innerRadius * 2, innerRadius * 2);
            }
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void expand(double amount) {
        width += amount;
        x -= amount / 2;
        constrainToBounds();
    }

    public void activateExpand(double amount, double durationSeconds) {
        if (expandTimerSeconds <= 0.0) {
            expand(amount);
        }
        expandTimerSeconds = Math.max(0.0, durationSeconds);
    }

    public void resetSize() {
        double centerX = getCenterX();
        width = originalWidth;
        x = centerX - width / 2;
        constrainToBounds();
    }

    public void equipSkin(String skinName) {
        String resolved = (skinName != null && VALID_SKINS.contains(skinName)) ? skinName : DEFAULT_SKIN;
        this.currentSkin = resolved;
        this.paddleImage = getSkin(resolved);
    }

    public boolean isGunMode() {
        return gunMode;
    }

    public void setGunMode(boolean mode) {
        this.gunMode = mode;
        if (!mode) {
            this.gunTimerSeconds = 0.0;
        }
    }

    public void activateGun(double durationSeconds) {
        this.gunMode = true;
        this.gunTimerSeconds = Math.max(0.0, durationSeconds);
    }

    public double getGunTimerSeconds() {
        return gunTimerSeconds;
    }

    public void setGunTimerSeconds(double gunTimerSeconds) {
        this.gunTimerSeconds = Math.max(0.0, gunTimerSeconds);
    }

    public double getExpandTimerSeconds() {
        return expandTimerSeconds;
    }

    public void setExpandTimerSeconds(double expandTimerSeconds) {
        this.expandTimerSeconds = Math.max(0.0, expandTimerSeconds);
    }

    public double getLeftGunX() {
        return x + 2;
    }

    public double getRightGunX() {
        return x + width - 12;
    }

    public double getGunY() {
        return y - 10;
    }

    public void triggerHitFlash(double duration, double collisionX, double collisionY) {
        this.hitFlashTime = duration;
        this.collisionX = collisionX;
        this.collisionY = collisionY;
    }
}
