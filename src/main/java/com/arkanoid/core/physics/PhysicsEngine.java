package com.arkanoid.core.physics;

public class PhysicsEngine {
    private static final double GRAVITY = 0;
    
    public static void applyGravity(double velocityY, double deltaTime) {
    }
    
    public static double[] reflect(double velocityX, double velocityY, double normalX, double normalY) {
        double dotProduct = velocityX * normalX + velocityY * normalY;
        double reflectedX = velocityX - 2 * dotProduct * normalX;
        double reflectedY = velocityY - 2 * dotProduct * normalY;
        return new double[]{reflectedX, reflectedY};
    }
    
    public static double calculateSpeed(double velocityX, double velocityY) {
        return Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }
    
    public static double[] normalizeVelocity(double velocityX, double velocityY, double targetSpeed) {
        double currentSpeed = calculateSpeed(velocityX, velocityY);
        if (currentSpeed == 0) return new double[]{0, 0};
        
        double ratio = targetSpeed / currentSpeed;
        return new double[]{velocityX * ratio, velocityY * ratio};
    }
}
