package com.arkanoid.utils;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorCacheTest {

    @BeforeEach
    void setUp() {
        ColorCache.clear();
    }

    @AfterEach
    void tearDown() {
        ColorCache.clear();
    }

    @Test
    void testGetColorCachesSameInstance() {
        Color c1 = ColorCache.getColor(0.5, 0.25, 0.75, 0.8);
        Color c2 = ColorCache.getColor(0.5, 0.25, 0.75, 0.8);
        assertSame(c1, c2);
        assertEquals(1, ColorCache.size());
    }

    @Test
    void testGetWithAlphaPreservesRgbAndAppliesOpacity() {
        Color base = Color.rgb(51, 102, 204);
        Color faded = ColorCache.getWithAlpha(base, 0.5);

        assertEquals(51 / 255.0, faded.getRed(), 0.01);
        assertEquals(102 / 255.0, faded.getGreen(), 0.01);
        assertEquals(204 / 255.0, faded.getBlue(), 0.01);
        assertEquals(0.5, faded.getOpacity(), 0.01);
    }

    @Test
    void testCacheIsBoundedAtMaxSize() {
        for (int i = 0; i < 2500; i++) {
            int r = (i >> 8) & 0xFF;
            int g = (i >> 4) & 0xFF;
            int b = i & 0xFF;
            ColorCache.getColor(r / 255.0, g / 255.0, b / 255.0, 1.0);
        }
        assertTrue(ColorCache.size() <= ColorCache.MAX_CACHE_SIZE,
                "Cache size exceeded MAX_CACHE_SIZE: " + ColorCache.size());
    }

    @Test
    void testClampsOutOfRangeAndNonFiniteInputs() {
        Color clamped = ColorCache.getColor(-0.5, 2.0, Double.NaN, 1.5);
        assertEquals(0.0, clamped.getRed(), 0.001);
        assertEquals(1.0, clamped.getGreen(), 0.001);
        assertEquals(0.0, clamped.getBlue(), 0.001);
        assertEquals(1.0, clamped.getOpacity(), 0.001);
    }
}
