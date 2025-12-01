package com.arkanoid.core.entities;

import com.arkanoid.systems.logging.GameLogger;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.slf4j.Logger;

public abstract class BaseBrick extends GameObject implements Brick {
    private static final Logger logger = GameLogger.getLogger(BaseBrick.class);
    protected int hitPoints;
    protected int maxHitPoints;
    protected Color color;
    protected boolean destroyed;
    protected double powerUpChance;
    protected int row;
    protected int col;
    protected Image texture;
    protected String texturePath;

    public BaseBrick(double x, double y, double width, double height,
            int hitPoints, Color color, int row, int col) {
        this(x, y, width, height, hitPoints, color, row, col, null);
    }

    public BaseBrick(double x, double y, double width, double height, int hitPoints, Color color, int row, int col,
            String texturePath) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.color = color;
        this.destroyed = false;
        this.powerUpChance = 0.5;
        this.col = col;
        this.row = row;
        this.texturePath = texturePath;
        if (texturePath != null) {
            this.texture = new Image(getClass().getResourceAsStream(texturePath));
        }
    }

    @Override
    public void hit() {
        hitPoints--;
        if (hitPoints <= 0) {
            logger.debug("Brick destroyed at ({}, {}) row={} col={}", String.format("%.2f", x), String.format("%.2f", y), row, col);
            destroyed = true;
            active = false;
        }
    }

    @Override
    public void destroy() {
        logger.debug("Brick.destroy() called at ({}, {}) row={} col={}", String.format("%.2f", x), String.format("%.2f", y), row, col);
        destroyed = true;
        active = false;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void render(GraphicsContext gc) {
        if (destroyed)
            return;

        if (texture != null) {
            gc.drawImage(texture, x, y, width, height);
        } else {
            double brightness = (double) hitPoints / maxHitPoints;
            Color renderColor = color.deriveColor(0, 1, brightness, 1);
            gc.setFill(renderColor);
            gc.fillRect(x, y, width, height);
        }
    }

    @Override
    public PowerUp dropPowerUp() {
        if (Math.random() < powerUpChance) {
            PowerUp powerUp = createRandomPowerUp();
            logger.debug("PowerUp dropped: {} at ({}, {})", powerUp.getClass().getSimpleName(), String.format("%.2f", getCenterX()), String.format("%.2f", getCenterY()));
            return powerUp;
        }
        return null;
    }

    @Override
    public void instantDestroy() {
        logger.debug("Brick instantly destroyed at ({}, {}) row={} col={}", String.format("%.2f", x), String.format("%.2f", y), row, col);
        this.destroyed = true;
        this.active = false;
        this.hitPoints = 0;
    }

    private PowerUp createRandomPowerUp() {
        double random = Math.random();
        double centerX = getCenterX();
        double centerY = getCenterY();

        if (random < 0.25) { // range [0,0.25) ~ 25%
            return new RowClearPowerUp(centerX, centerY);
        } else if (random < 0.5) { // range [0.25,0.5) ~ 25%
            return new ExplosiveBallPowerUp(centerX, centerY);
        } else if (random < 0.75) { // range [0.5,0.75) ~ 25%
            return new GunPaddlePowerUp(centerX, centerY);
        } else { // range [0.75,1) ~ 25%
            return new ExpandPaddlePowerUp(centerX, centerY);
        }

    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getTexturePath() {
        return texturePath;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
