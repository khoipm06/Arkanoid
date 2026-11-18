package com.arkanoid.ui.view;

import com.arkanoid.core.GameConstants;
import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewportScalerTest {

    @Test
    void testWrapLocksRegionDesignDimensionsAndComputesDefaultScale() {
        AnchorPane content = new AnchorPane();
        double designWidth = 868.0;
        double designHeight = 597.0;

        StackPane wrapper = ViewportScaler.wrap(content, designWidth, designHeight);

        assertEquals(GameConstants.WINDOW_WIDTH, wrapper.getPrefWidth(), 1e-6);
        assertEquals(GameConstants.WINDOW_HEIGHT, wrapper.getPrefHeight(), 1e-6);
        assertSame(content, wrapper.getChildren().get(0));

        assertEquals(designWidth, content.getMinWidth(), 1e-6);
        assertEquals(designWidth, content.getPrefWidth(), 1e-6);
        assertEquals(designWidth, content.getMaxWidth(), 1e-6);
        assertEquals(designHeight, content.getMinHeight(), 1e-6);
        assertEquals(designHeight, content.getPrefHeight(), 1e-6);
        assertEquals(designHeight, content.getMaxHeight(), 1e-6);

        assertEquals(GameConstants.WINDOW_WIDTH / designWidth, content.getScaleX(), 1e-6);
        assertEquals(GameConstants.WINDOW_HEIGHT / designHeight, content.getScaleY(), 1e-6);
    }

    @Test
    void testWrapUpdatesScaleWhenWrapperResizes() {
        AnchorPane content = new AnchorPane();
        StackPane wrapper = ViewportScaler.wrap(content, 800.0, 600.0);

        wrapper.resize(1600.0, 1200.0);

        assertEquals(2.0, content.getScaleX(), 1e-6);
        assertEquals(2.0, content.getScaleY(), 1e-6);
    }

    @Test
    void testWrapSupportsNonRegionParentAndFallsBackOnInvalidDimensions() {
        Group nonRegion = new Group();
        StackPane wrapper = ViewportScaler.wrap(nonRegion, -1.0, 0.0);

        assertSame(nonRegion, wrapper.getChildren().get(0));
        assertEquals(GameConstants.WINDOW_WIDTH / GameConstants.DEFAULT_DESIGN_WIDTH, nonRegion.getScaleX(), 1e-6);
        assertEquals(GameConstants.WINDOW_HEIGHT / GameConstants.DEFAULT_DESIGN_HEIGHT, nonRegion.getScaleY(), 1e-6);
    }

    @Test
    void testWrapRejectsNullContent() {
        assertThrows(IllegalArgumentException.class, () -> ViewportScaler.wrap(null, 800.0, 600.0));
    }
}
