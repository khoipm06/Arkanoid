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
        this.powerUpChance = 0.5;
        this.col = col;
        this.row = row;
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
        if(destroyed) return;
            double brightness = (double) hitPoints / maxHitPoints;
            Color renderColor = color.deriveColor(0, 1, brightness, 1);
            gc.setFill(renderColor);
            gc.fillRect(x, y, width, height);

        gc.setLineWidth(1.0);
            gc.setStroke(Color.DARKGRAY);
        gc.strokeRect(x + 0.5, y + 0.5, Math.max(0, width - 1), Math.max(0, height - 1));

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
        
//        if (random < 0.4) {
//            return new ExpandPaddlePowerUp(centerX, centerY);
//        } else if (random < 0.7) {
//            return new MultiBallPowerUp(centerX, centerY);
//        } else {
//            return new ExplosiveBallPowerUp(centerX, centerY);
//        }
//        return new GunPaddlePowerUp(centerX, centerY);
        if (random < 0.5 ) {
            return new ExplosiveBallPowerUp(centerX, centerY);
        }
        return new RowClearPowerUp(centerX, centerY);
       }
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
