package com.arkanoid.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Simple toast notification utility for displaying temporary messages.
 */
public class ToastNotification {

    /**
     * Shows a toast message on the specified parent region.
     * 
     * @param message the message to display
     * @param parent  the parent region to display the toast on
     */
    public static void showToast(String message, Region parent) {
        showToast(message, parent, ToastType.INFO);
    }

    /**
     * Shows a toast message with a specific type.
     * 
     * @param message the message to display
     * @param parent  the parent region to display the toast on
     * @param type    the type of toast (INFO, SUCCESS, ERROR)
     */
    public static void showToast(String message, Region parent, ToastType type) {
        // Create toast label
        Label toastLabel = new Label(message);
        toastLabel.setStyle(getStyleForType(type));
        toastLabel.setWrapText(true);
        toastLabel.setMaxWidth(400);

        // Create container
        StackPane toastPane = new StackPane(toastLabel);
        toastPane.setAlignment(Pos.BOTTOM_CENTER);
        toastPane.setStyle("-fx-padding: 20;");
        toastPane.setMouseTransparent(true);

        // Add to parent if it's a StackPane, otherwise wrap in one
        if (parent instanceof StackPane) {
            StackPane stackParent = (StackPane) parent;
            stackParent.getChildren().add(toastPane);

            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            // Wait
            PauseTransition pause = new PauseTransition(Duration.seconds(3));

            // Fade out
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastPane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> stackParent.getChildren().remove(toastPane));

            // Chain animations
            fadeIn.setOnFinished(e -> pause.play());
            pause.setOnFinished(e -> fadeOut.play());
            fadeIn.play();
        }
    }

    private static String getStyleForType(ToastType type) {
        String baseStyle = "-fx-background-color: %s;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 15px 25px;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 3);";

        switch (type) {
            case SUCCESS:
                return String.format(baseStyle, "linear-gradient(to right, #00b09b, #96c93d)");
            case ERROR:
                return String.format(baseStyle, "linear-gradient(to right, #eb3349, #f45c43)");
            case WARNING:
                return String.format(baseStyle, "linear-gradient(to right, #f2994a, #f2c94c)");
            case INFO:
            default:
                return String.format(baseStyle, "linear-gradient(to right, #4facfe, #00f2fe)");
        }
    }

    public enum ToastType {
        INFO,
        SUCCESS,
        ERROR,
        WARNING
    }
}
