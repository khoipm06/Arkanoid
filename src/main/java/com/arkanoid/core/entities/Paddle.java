package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class Paddle extends MovableObject {
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
    private Image defaultPaddleImage;
    private final String originalSkin = "paddle_Default";
    private boolean gunMode = false;
    private long gunExpiryNano = -1;
    private Image gunImage;

    public Paddle(double x, double y, double width, double height) {
        super(x, y, height, width, 0);
        this.originalWidth = width;
    }

    public Paddle(double x, double y, double width, double height, double speed, double minX, double maxX) {
        super(x, y, width, height, speed);
        this.minX = minX;
        this.maxX = maxX;
        this.originalWidth = width;
        this.paddleImage = getSkin(currentSkin);
        this.defaultPaddleImage = this.paddleImage;
        try (InputStream stream = Paddle.class.getResourceAsStream("/images/gun.png")) {
            if (stream != null) gunImage = new Image(stream);
            else System.err.println("Không tìm thấy ảnh gun.png");
        } catch (Exception e) {
            System.err.println("Lỗi load gun.png: " + e.getMessage());
        }

    }

    private static void loadSkins() {
        String[] skinNames = {"paddle_Default", "paddle_Wood", "paddle_Metal", "paddle_Neon"};
        for (String name : skinNames) {
            try (InputStream stream = Paddle.class.getResourceAsStream("/images/" + name + ".png")) {
                if (stream != null) {
                    paddleSkins.put(name, new Image(stream));
                } else {
                    System.err.println("Không tìm thấy ảnh paddle_" + name + ".png");
                }
            } catch (Exception e) {
                System.err.println("Lỗi load skin " + name + ": " + e.getMessage());
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
            System.err.println("Không tồn tại skin: " + skinName);
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
        if (x < minX) x = minX;
        if (x + width > maxX) x = maxX - width;
    }

    @Override
    public void update(double deltaTime) {

        constrainToBounds();
        if (gunMode && gunExpiryNano != -1 && System.nanoTime() > gunExpiryNano) {
            gunMode = false;
        }
        // nếu skin tạm hết hạn → trả về skin gốc
        if (!currentSkin.equals(originalSkin)) {
            this.paddleImage = getSkin(originalSkin);
            currentSkin = originalSkin;
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
        return x + 1; // offset nhỏ để đạn không nằm sát viền
    }

    public double getRightGunX() {
        return x + width - 12; // offset: width - margin - bulletWidth
    }

    public double getGunY() {
        return y - 10; // phía trên paddle
    }

}
