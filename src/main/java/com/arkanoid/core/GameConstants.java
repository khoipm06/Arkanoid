package com.arkanoid.core;

/**
 * Canonical application, viewport, and gameplay constants shared across systems and UI.
 */
public final class GameConstants {

    private GameConstants() {
        // Constants class
    }

    // Window & Viewport Dimensions
    public static final double WINDOW_WIDTH = 1000.0;
    public static final double WINDOW_HEIGHT = 650.0;

    public static final double SINGLE_PLAYER_CANVAS_WIDTH = 700.0;
    public static final double SINGLE_PLAYER_PANEL_WIDTH = 300.0;

    public static final double TWO_PLAYER_DESIGN_WIDTH = 900.0;
    public static final double TWO_PLAYER_CANVAS_WIDTH = 700.0;
    public static final double TWO_PLAYER_CANVAS_HEIGHT = 600.0;
    public static final double TWO_PLAYER_PANEL_WIDTH = 200.0;

    public static final double DEFAULT_DESIGN_WIDTH = 868.0;
    public static final double DEFAULT_DESIGN_HEIGHT = 597.0;

    // Simulation Timing
    public static final double MAX_FRAME_DELTA = 0.05;
    public static final double MAX_PHYSICS_STEP = 1.0 / 120.0;

    // Gameplay & Entity Defaults
    public static final double DEFAULT_BALL_RADIUS = 8.0;
    public static final double DEFAULT_BALL_SPEED = 300.0;
    public static final double DEFAULT_PADDLE_SPEED = 400.0;
    public static final double DEFAULT_PADDLE_WIDTH = 100.0;
    public static final double DEFAULT_PADDLE_HEIGHT = 25.0;
    public static final double DEFAULT_POWERUP_DURATION_SECONDS = 2.0;

    public static final double GUN_FIRE_INTERVAL = 0.2;
    public static final double DEFAULT_BULLET_WIDTH = 6.0;
    public static final double DEFAULT_BULLET_HEIGHT = 12.0;
    public static final double DEFAULT_BULLET_SPEED = -300.0;

    public static final double TRAIL_SPAWN_INTERVAL = 0.015;
    public static final int MAX_TRAIL_EFFECTS = 300;
    public static final int MAX_PARTICLES = 300;
    public static final int MAX_BULLETS = 50;
    public static final int MAX_FLOATING_TEXTS = 30;

    public static final int MAX_LEVEL = 3;
}
