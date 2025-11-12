package com.arkanoid.core.entities;

import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Ball extends MovableObject {
    private static final Logger logger = GameLogger.getLogger(Ball.class);
    private static final SoundManager soundManager = SoundManager.getInstance();
    private static final Map<String, Image> SKINS = new HashMap<>();
    private static String currentSkin = "Default";

    static {
        loadSkins();
    }

    private final double radius;
    private Color color;
    private double minX, minY, maxX, maxY;
    private boolean attachedToPaddle = false;
    private Image ballImage;
    private boolean explosive = false;
    private boolean hasExploded = false;
    private boolean topIsDeadSide = false; // For two-player mode: top player's drain

    public Ball(double x, double y, double radius, double speed) {
        super(x - radius, y - radius, radius * 2, radius * 2, speed);
        this.radius = radius;
        this.color = Color.RED;
        this.velocityX = 0;
        this.velocityY = 0;
        attachedToPaddle = true;
        equipSkin(currentSkin);
    }

    private static void loadSkins() {
        String[] skinNames = { "Fire", "Ice", "Rainbow", "Default" };
        for (String name : skinNames) {
            try (InputStream stream = Ball.class.getResourceAsStream("/images/" + name + ".png")) {
                if (stream != null) {
                    SKINS.put(name, new Image(stream));
                } else {
                    logger.warn("Could not find image: {}.png", name);
                }
            } catch (Exception e) {
                logger.error("Error loading skin {}: {}", name, e.getMessage());
            }
        }

        // If Default skin can't be loaded, create a default circle
        if (!SKINS.containsKey("Default")) {
            SKINS.put("Default", null);
        }
    }

    public static Image getSkin(String skinName) {
        return SKINS.getOrDefault(skinName, SKINS.get("Default"));
    }

    public static String getCurrentSkin() {
        return currentSkin;
    }

    public static void setCurrentSkin(String skinName) {
        if (SKINS.containsKey(skinName)) {
            currentSkin = skinName;
            logger.info("Current ball skin: {}", skinName);
        } else {
            logger.warn("Skin does not exist: {}", skinName);
        }
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
        
        // Top boundary: bounce only if it's NOT player 2's dead side
        if (y <= minY && !topIsDeadSide) {
            y = minY;
            reverseY();
        }
        
        // Bottom boundary: bounce only if it's NOT player 1's dead side
        // (topIsDeadSide == true means this is player 2's ball, so bottom should bounce)
        if (y + height >= maxY && topIsDeadSide) {
            y = maxY - height;
            reverseY();
        }
    }

    public void checkPaddleCollision(Paddle paddle) {
        if (!intersects(paddle)) {
            return;
        }

        // Bottom paddle (player 1): ball coming from above (velocityY > 0)
        // Top paddle (player 2): ball coming from below (velocityY < 0)
        boolean isBottomPaddleHit = !topIsDeadSide && velocityY > 0;
        boolean isTopPaddleHit = topIsDeadSide && velocityY < 0;

        if (isBottomPaddleHit || isTopPaddleHit) {
            logger.debug("Paddle collision: topIsDeadSide={}, velocityY={}, isBottom={}, isTop={}", 
                topIsDeadSide, velocityY, isBottomPaddleHit, isTopPaddleHit);
            soundManager.playSound("paddleBounce.wav");
            paddle.triggerHitFlash(0.2);

            // Position ball at correct side of paddle
            if (isBottomPaddleHit) {
                y = paddle.getY() - height; // Above paddle
            } else {
                y = paddle.getY() + paddle.getHeight(); // Below paddle
            }

            double hitPosition = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() / 2);
            velocityX = hitPosition * speed * 0.8;
            
            // Reverse Y direction based on paddle type
            if (isBottomPaddleHit) {
                velocityY = -Math.abs(velocityY); // Bounce up
            } else {
                velocityY = Math.abs(velocityY); // Bounce down
            }

            double totalSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (totalSpeed != speed) {
                double ratio = speed / totalSpeed;
                velocityX *= ratio;
                velocityY *= ratio;
            }
        }
    }

    public void equipSkin(String skinName) {
        Image skin = SKINS.getOrDefault(skinName, SKINS.get("Default"));
        this.ballImage = skin;
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
        // Player 1 (bottom player): loses life if ball goes past bottom (y > maxY)
        if (!topIsDeadSide && y > maxY) {
            return true;
        }
        // Player 2 (top player): loses life if ball goes past top (y < minY)
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
