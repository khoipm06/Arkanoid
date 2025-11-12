package com.arkanoid.core.entities;

import com.arkanoid.systems.logging.GameLogger;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Paddle extends MovableObject {
    private static final Logger logger = GameLogger.getLogger(Paddle.class);
    private static final Map<String, Image> paddleSkins = new HashMap<>();
    private static String currentSkin = "paddle_Default";

    static {
        loadSkins();
    }

    private final double originalWidth;
    private double minX;
    private double maxX;
    private Color color;
    private Image paddleImage;
    private String equippedSkin = "paddle_Default"; // The skin equipped by the player
                                                    // but now initially set to default
    private boolean gunMode = false;
    private long gunExpiryNano = -1;
    private Image gunImage;
    private double collisionX;
    private double collisionY;
    private double hitFlashTime = 0.0; // New field for hit flash effect

    public Paddle(double x, double y, double width, double height) {
        super(x, y, width, height, 0);
        this.originalWidth = width;
    }

    public Paddle(double x, double y, double width, double height, double speed, double minX, double maxX) {
        super(x, y, width, height, speed);
        this.minX = minX;
        this.maxX = maxX;
        this.originalWidth = width;
        this.paddleImage = getSkin(currentSkin);
        try (InputStream stream = Paddle.class.getResourceAsStream("/images/gun.png")) {
            if (stream != null)
                gunImage = new Image(stream);
            else
                logger.warn("Could not find the asset: gun.png");
        } catch (Exception e) {
            logger.error("Error loading gun.png: {}", e.getMessage());
        }

    }

    private static void loadSkins() {
        String[] skinNames = { "paddle_Default", "paddle_Wood", "paddle_Metal", "paddle_Neon" };
        for (String name : skinNames) {
            try (InputStream stream = Paddle.class.getResourceAsStream("/images/" + name + ".png")) {
                if (stream != null) {
                    paddleSkins.put(name, new Image(stream));
                } else {
                    logger.warn("Could not find the asset: paddle_{}.png", name);
                }
            } catch (Exception e) {
                logger.error("Error loading skin {}: {}", name, e.getMessage());
            }
        }
    }

    public static Image getSkin(String skinName) {
        return paddleSkins.getOrDefault(skinName, paddleSkins.get("paddle_Default"));
    }

    public static String getCurrentSkin() {
        return currentSkin;
    }

    public static void setCurrentSkin(String skinName) {
        if (paddleSkins.containsKey(skinName)) {
            currentSkin = skinName;
        } else {
            logger.warn("Could not find skin: {}", skinName);
        }
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
        if (x < minX)
            x = minX;
        if (x + width > maxX)
            x = maxX - width;
    }

    @Override
    public void update(double deltaTime) {
        constrainToBounds();
        if (gunMode && gunExpiryNano != -1 && System.nanoTime() > gunExpiryNano) {
            gunMode = false;
        }
        // reset if current skin  differs from equipped skin
        if (!currentSkin.equals(equippedSkin)) {
            this.paddleImage = getSkin(equippedSkin);
            currentSkin = equippedSkin;
        }
        if (hitFlashTime > 0) { // Update hit flash timer
            hitFlashTime -= deltaTime;
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

        if (hitFlashTime > 0) {
            double progress = hitFlashTime / 0.4;
            double intensity = Math.pow(progress, 0.7);
            double maxRadius = width;
            double currentRadius = maxRadius * (1 - progress);

            RadialGradient gradient = new RadialGradient(
                    0, 0,
                    collisionX, collisionY,
                    currentRadius,
                    false,
                    CycleMethod.NO_CYCLE,
                    // new Stop(0, Color.color(1, 1, 1, intensity)),
                    // new Stop(0.4, Color.color(0.5, 0.8, 1.0, intensity * 0.4)),
                    // new Stop(1, Color.color(0.3, 0.6, 0.9, 0)));
                    new Stop(0, Color.color(1, 1, 1, intensity)),
                    new Stop(0.4, Color.color(0.2, 0.9, 1.0, intensity * 0.6)),
                    new Stop(1, Color.color(0.0, 0.5, 1.0, 0)));


            gc.setFill(gradient);
            // Only fill the immediate area around the collision
            gc.fillOval(collisionX - currentRadius, collisionY - currentRadius,
                    currentRadius * 2, currentRadius * 2);
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

    public void resetSize() {
        double centerX = getCenterX();
        width = originalWidth;
        x = centerX - width / 2;
        constrainToBounds();
    }

    public void equipSkin(String skinName) {
        Image skin = getSkin(skinName);
        this.paddleImage = skin;
    }

    public boolean isGunMode() {
        return gunMode && (gunExpiryNano == -1 || System.nanoTime() <= gunExpiryNano);
    }

    public void setGunMode(boolean mode) {
        this.gunMode = mode;
        if (!mode) {
            this.gunExpiryNano = -1;
        }
    }

    public long getGunExpiry() {
        return gunExpiryNano;
    }

    public void setGunExpiry(long expiryNano) {
        this.gunExpiryNano = expiryNano;
    }

    public double getLeftGunX() {
        return x + 2; // prevent the left gun is in the left margin
    }

    public double getRightGunX() {
        return x + width - 12; // offset: width - margin - bulletWidth
    }

    public double getGunY() {
        return y - 10; // above the paddle
    }

    public void triggerHitFlash(double duration, double collisionX, double collisionY) {
        this.hitFlashTime = duration;
        this.collisionX = collisionX;
        this.collisionY = collisionY;
    }
}
