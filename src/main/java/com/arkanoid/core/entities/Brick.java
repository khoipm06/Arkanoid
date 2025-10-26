package com.arkanoid.core.entities;

import javafx.scene.canvas.GraphicsContext;

public interface Brick {
    void hit();
    boolean isDestroyed();
    void update(double deltaTime);
    void render(GraphicsContext gc);
    
    double getX();
    double getY();
    double getWidth();
    double getHeight();
    
    boolean intersects(GameObject other);
    PowerUp dropPowerUp();
}
