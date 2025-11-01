package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class Paddle extends MovableObject {
    private double minX;
    private double maxX;
    private Color color;
    private final double originalWidth;
    private Image paddleImage;
    private static final Map<String, Image> paddleSkins = new HashMap<>();
    private static String currentSkin = "paddle_Default";

    static {
        loadSkins();
    }

    public Paddle(double x, double y, double width, double height) {
        super(x, y, height, width , 0);
        this.originalWidth = width;
    }
    public Paddle(double x, double y, double width, double height, double speed, double minX, double maxX) {
        super(x, y, width, height, speed);
        this.minX = minX;
        this.maxX = maxX;
        this.color = Color.BLUE;
        this.originalWidth = width;
        this.paddleImage = getSkin(currentSkin);

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
        double centerX = getCenterX(); // Giữ nguyên vị trí giữa
        width = originalWidth;
        x = centerX - width / 2;
        constrainToBounds();
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

    public void equipSkin(String skinName) {
        Image skin = getSkin(skinName);
        this.paddleImage = skin;
    }

    public static void setCurrentSkin(String skinName) {
        if (paddleSkins.containsKey(skinName)) {
            currentSkin = skinName;
        } else {
            System.err.println("Không tồn tại skin: " + skinName);
        }
    }

    public static String getCurrentSkin() {
        return currentSkin;
    }

}
