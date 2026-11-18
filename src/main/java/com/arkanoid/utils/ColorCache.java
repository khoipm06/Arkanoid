package com.arkanoid.utils;

import javafx.scene.paint.Color;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lock-free bounded cache for {@link Color} instances keyed by packed 32-bit RGBA integers.
 * Avoids per-frame string formatting and unbounded cache growth.
 */
public final class ColorCache {
    public static final int MAX_CACHE_SIZE = 2048;
    private static final ConcurrentHashMap<Integer, Color> cache = new ConcurrentHashMap<>();

    private ColorCache() {
        // Utility class
    }

    /**
     * Gets a cached color with the specified normalized RGBA components in [0, 1].
     *
     * @param r       Red component [0, 1]
     * @param g       Green component [0, 1]
     * @param b       Blue component [0, 1]
     * @param opacity Opacity component [0, 1]
     * @return Cached Color instance
     */
    public static Color getColor(double r, double g, double b, double opacity) {
        int r8 = toByte(r);
        int g8 = toByte(g);
        int b8 = toByte(b);
        int a8 = toByte(opacity);
        int key = ((r8 & 0xFF) << 24) | ((g8 & 0xFF) << 16) | ((b8 & 0xFF) << 8) | (a8 & 0xFF);

        Color cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Color created = new Color(r8 / 255.0, g8 / 255.0, b8 / 255.0, a8 / 255.0);
        synchronized (cache) {
            Color existing = cache.get(key);
            if (existing != null) {
                return existing;
            }
            if (cache.size() >= MAX_CACHE_SIZE) {
                cache.clear();
            }
            cache.put(key, created);
            return created;
        }
    }

    /**
     * Gets a cached version of a base color with a new opacity value.
     *
     * @param base    Base color (defaults to white if null)
     * @param opacity Opacity component [0, 1]
     * @return Cached Color instance
     */
    public static Color getWithAlpha(Color base, double opacity) {
        Color resolved = base != null ? base : Color.WHITE;
        return getColor(resolved.getRed(), resolved.getGreen(), resolved.getBlue(), opacity);
    }

    /**
     * Clears the color cache.
     */
    public static void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }

    /**
     * Returns the current number of cached color entries.
     */
    public static int size() {
        return cache.size();
    }

    private static int toByte(double value) {
        double clamped = Math.max(0.0, Math.min(1.0, Double.isFinite(value) ? value : 0.0));
        return (int) Math.round(clamped * 255.0);
    }
}
