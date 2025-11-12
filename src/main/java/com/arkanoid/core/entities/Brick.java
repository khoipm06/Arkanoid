package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
    Color getColor();
    
    boolean intersects(GameObject other);
    PowerUp dropPowerUp();
    void instantDestroy();
}
