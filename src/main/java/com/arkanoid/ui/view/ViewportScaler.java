package com.arkanoid.ui.view;

import com.arkanoid.core.GameConstants;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Wraps any UI {@link Parent} authored at a fixed design resolution into a unified
 * {@code 1000 x 650} viewport container while keeping local node coordinates and CSS
 * button hitboxes pixel-aligned.
 */
public final class ViewportScaler {
    private static final String VIEWPORT_BACKGROUND_STYLE = "-fx-background-color: #0a0e1a;";

    private ViewportScaler() {
    }

    /**
     * Wraps {@code content} in a centered {@link StackPane} sized to
     * {@link GameConstants#WINDOW_WIDTH} x {@link GameConstants#WINDOW_HEIGHT} and scales it
     * proportionally to fill the wrapper without distorting FXML layout coordinates.
     *
     * @param content      the root node designed at {@code designWidth x designHeight}
     * @param designWidth  authored design width in pixels
     * @param designHeight authored design height in pixels
     * @return the {@link StackPane} viewport wrapper
     */
    public static StackPane wrap(Parent content, double designWidth, double designHeight) {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null");
        }
        double targetDesignWidth = (Double.isFinite(designWidth) && designWidth > 0.0)
                ? designWidth
                : GameConstants.DEFAULT_DESIGN_WIDTH;
        double targetDesignHeight = (Double.isFinite(designHeight) && designHeight > 0.0)
                ? designHeight
                : GameConstants.DEFAULT_DESIGN_HEIGHT;

        if (content instanceof Region region) {
            region.setMinSize(targetDesignWidth, targetDesignHeight);
            region.setPrefSize(targetDesignWidth, targetDesignHeight);
            region.setMaxSize(targetDesignWidth, targetDesignHeight);
        }

        StackPane wrapper = new StackPane(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        wrapper.setStyle(VIEWPORT_BACKGROUND_STYLE);

        content.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    double w = wrapper.getWidth();
                    return (w > 0.0 ? w : GameConstants.WINDOW_WIDTH) / targetDesignWidth;
                },
                wrapper.widthProperty()
        ));
        content.scaleYProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    double h = wrapper.getHeight();
                    return (h > 0.0 ? h : GameConstants.WINDOW_HEIGHT) / targetDesignHeight;
                },
                wrapper.heightProperty()
        ));

        return wrapper;
    }
}
