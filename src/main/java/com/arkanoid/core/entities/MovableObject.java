package com.arkanoid.core.entities;

public abstract class MovableObject extends GameObject {
    protected double velocityX;
    protected double velocityY;
    protected double speed;

    public MovableObject(double x, double y, double width, double height, double speed) {
        super(x, y, width, height);
        this.speed = speed;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    public void move(double deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
    }

    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getSpeed() { return speed; }
    
    public void setVelocityX(double velocityX) { this.velocityX = velocityX; }
    public void setVelocityY(double velocityY) { this.velocityY = velocityY; }
    public void setSpeed(double speed) { this.speed = speed; }

    public void reverseX() { velocityX = -velocityX; }
    public void reverseY() { velocityY = -velocityY; }
}
