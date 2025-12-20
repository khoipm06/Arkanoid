package com.arkanoid.utils;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for caching Color objects to reduce memory churn.
 * Especially useful for colors with changing alpha values (fade effects).
 */
public class ColorCache {
    // Key format: "r-g-b-a" (e.g., "1.0-0.0-0.0-0.5")
    private static final Map<String, Color> cache = new HashMap<>();
    
    /**
     * Gets a cached color with the specified RGBA values.
     * Alpha is rounded to 2 decimal places to increase cache hits.
     */
    public static Color getColor(double r, double g, double b, double a) {
        // Clamp alpha
        double roundedA = Math.max(0, Math.min(1, a));
        // Round to 2 decimal places (e.g. 0.543 -> 0.54)
        roundedA = Math.round(roundedA * 100.0) / 100.0;
        
        String key = String.format("%.2f-%.2f-%.2f-%.2f", r, g, b, roundedA);

        double finalRoundedA = roundedA;
        return cache.computeIfAbsent(key, k -> new Color(r, g, b, finalRoundedA));
    }
    /**
     * Gets a cached version of a base color with a new alpha value.
     */
    public static Color getWithAlpha(Color base, double alpha) {
        return getColor(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    /**
     * Clears the cache. Useful when changing levels or resetting game.
     */
    public static void clear() {
        cache.clear();
    }
}
