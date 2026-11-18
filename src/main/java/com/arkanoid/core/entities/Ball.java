package com.arkanoid.core.entities;

import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.utils.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.slf4j.Logger;

import java.util.Set;

public class Ball extends MovableObject {
    private static final Logger logger = GameLogger.getLogger(Ball.class);
    private static final SoundManager soundManager = SoundManager.getInstance();
    private static final String DEFAULT_SKIN = "Default";
    private static final Set<String> VALID_SKINS = Set.of("Fire", "Ice", "Rainbow", "Default");
    private static volatile String currentSkin = DEFAULT_SKIN;

    private final double radius;
    private Color color;
    private double minX, minY, maxX, maxY;
    private boolean attachedToPaddle = false;
    private Image ballImage;
    private String equippedSkin;
    private boolean explosive = false;
    private boolean hasExploded = false;
    private boolean topIsDeadSide = false;

    public Ball(double x, double y, double radius, double speed) {
        super(x - radius, y - radius, radius * 2, radius * 2, speed);
        this.radius = radius;
        this.color = Color.RED;
        this.velocityX = 0;
        this.velocityY = 0;
        this.attachedToPaddle = true;
        equipSkin(currentSkin);
    }

    public static Image getSkin(String skinName) {
        String resolved = (skinName != null && VALID_SKINS.contains(skinName)) ? skinName : DEFAULT_SKIN;
        Image img = ImageLoader.loadImage("/images/" + resolved + ".png");
        if (img == null && !DEFAULT_SKIN.equals(resolved)) {
            img = ImageLoader.loadImage("/images/" + DEFAULT_SKIN + ".png");
        }
        return img;
    }

    public static String getCurrentSkin() {
        return currentSkin;
    }

    public static void setCurrentSkin(String skinName) {
        if (skinName != null && VALID_SKINS.contains(skinName)) {
            currentSkin = skinName;
            logger.info("Current ball skin: {}", skinName);
        } else {
            logger.warn("Skin does not exist: {}", skinName);
        }
    }

    public String getEquippedSkin() {
        return equippedSkin;
    }

    public void setBounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    @Override
    public void update(double deltaTime) {
        move(deltaTime);
        checkWallCollision();
    }

    public void launch() {
        if (attachedToPaddle) {
            attachedToPaddle = false;
            velocityY = -Math.abs(speed);
            velocityX = 0;
        }
    }

    private void checkWallCollision() {
        if (x <= minX) {
            x = minX;
            reverseX();
        }
        if (x + width >= maxX) {
            x = maxX - width;
            reverseX();
        }

        if (y <= minY && !topIsDeadSide) {
            y = minY;
            reverseY();
        }

        if (y + height >= maxY && topIsDeadSide) {
            y = maxY - height;
            reverseY();
        }
    }

    public void checkPaddleCollision(Paddle paddle) {
        if (!intersects(paddle)) {
            return;
        }

        boolean isBottomPaddleHit = !topIsDeadSide && velocityY > 0;
        boolean isTopPaddleHit = topIsDeadSide && velocityY < 0;

        if (isBottomPaddleHit || isTopPaddleHit) {
            logger.debug("Paddle collision: topIsDeadSide={}, velocityY={}, isBottom={}, isTop={}",
                    topIsDeadSide, velocityY, isBottomPaddleHit, isTopPaddleHit);
            soundManager.playSound("paddleBounce.wav");
            double hitY = isBottomPaddleHit ? paddle.getY() : paddle.getY() + paddle.getHeight();
            paddle.triggerHitFlash(0.4, getCenterX(), hitY);

            if (isBottomPaddleHit) {
                y = paddle.getY() - height;
            } else {
                y = paddle.getY() + paddle.getHeight();
            }

            double hitPosition = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() / 2);
            velocityX = hitPosition * speed * 0.8;

            if (isBottomPaddleHit) {
                velocityY = -Math.abs(velocityY);
            } else {
                velocityY = Math.abs(velocityY);
            }

            double totalSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (totalSpeed != speed && totalSpeed > 0) {
                double ratio = speed / totalSpeed;
                velocityX *= ratio;
                velocityY *= ratio;
            }
        }
    }

    public void equipSkin(String skinName) {
        String resolved = (skinName != null && VALID_SKINS.contains(skinName)) ? skinName : DEFAULT_SKIN;
        this.equippedSkin = resolved;
        this.ballImage = getSkin(resolved);
    }

    @Override
    public void render(GraphicsContext gc) {
        if (ballImage != null) {
            gc.drawImage(ballImage, x, y, width, height);
        } else {
            gc.setFill(color);
            gc.fillOval(x, y, width, height);
        }
    }

    public void setBallImage(Image image) {
        this.ballImage = image;
    }

    public boolean isOutOfBounds() {
        if (!topIsDeadSide && y > maxY) {
            return true;
        }
        if (topIsDeadSide && y + height < minY) {
            return true;
        }
        return false;
    }

    public void setTopIsDeadSide(boolean topIsDeadSide) {
        this.topIsDeadSide = topIsDeadSide;
    }

    public boolean isTopDeadSide() {
        return topIsDeadSide;
    }

    public void reset(double x, double y) {
        this.x = x - radius;
        this.y = y - radius;
        this.velocityX = speed * 0.7;
        this.velocityY = -speed;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isAttachedToPaddle() {
        return attachedToPaddle;
    }

    public void setAttachedToPaddle(boolean attachedToPaddle) {
        this.attachedToPaddle = attachedToPaddle;
    }

    public boolean hasExploded() {
        return hasExploded;
    }

    public void setHasExploded(boolean hasExploded) {
        this.hasExploded = hasExploded;
    }

    public boolean isExplosive() {
        return explosive;
    }

    public void setExplosive(boolean value) {
        explosive = value;
    }
}
