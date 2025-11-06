package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;

public interface Brick {
    void hit();
    void destroy();
    boolean isDestroyed();
    void update(double deltaTime);
    void render(GraphicsContext gc);
    
    double getX();
    double getY();
    double getWidth();
    double getHeight();
    double getCenterX();
    double getCenterY();
    int getRow();
    int getCol();
    
    boolean intersects(GameObject other);
    PowerUp dropPowerUp();
}
