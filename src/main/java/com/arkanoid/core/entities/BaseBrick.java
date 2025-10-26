package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class BaseBrick extends GameObject implements Brick {
    protected int hitPoints;
    protected int maxHitPoints;
    protected Color color;
    protected boolean destroyed;
    protected double powerUpChance;
    protected int row;
    protected int col;

    public BaseBrick(double x, double y, double width, double height, int hitPoints, Color color, int row, int col) {
        super(x, y, width, height);
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.color = color;
        this.destroyed = false;
        this.powerUpChance = 0.2;
        this.row = row;
        this.col = col;
    }

    @Override
    public void hit() {
        hitPoints--;
        if (hitPoints <= 0) {
            destroyed = true;
            active = false;
        }
    }

    @Override
    public void destroy() {
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
        if (!destroyed) {
            double brightness = (double) hitPoints / maxHitPoints;
            Color renderColor = color.deriveColor(0, 1, brightness, 1);
            gc.setFill(renderColor);
            gc.fillRect(x, y, width, height);
            gc.setStroke(Color.DARKGRAY);
            gc.strokeRect(x, y, width, height);
        }
    }

    @Override
    public PowerUp dropPowerUp() {
        if (Math.random() < powerUpChance) {
            return createRandomPowerUp();
        }
        return null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }


    private PowerUp createRandomPowerUp() {
        double random = Math.random();
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        if (random < 0.3) {
            return new ExpandPaddlePowerUp(centerX, centerY);
        } else if (random < 0.6) {
            return new MultiBallPowerUp(centerX, centerY);
        } else if (random < 0.8) {
            return new ExplosiveBallPowerUp(centerX, centerY);
        } else {
            return new RowClearPowerUp(centerX, centerY);
        }
    }
}
