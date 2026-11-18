package com.arkanoid.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Pause overlay displayed on top of single-player gameplay.
 */
public class GamePauseOverlay extends VBox {
    private static final String OVERLAY_STYLE =
            "-fx-background-color: rgba(0, 0, 0, 0.75);"
                    + "-fx-padding: 40;"
                    + "-fx-border-radius: 20;"
                    + "-fx-background-radius: 20;";

    private static final String NORMAL_BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298);"
                    + "-fx-text-fill: white;"
                    + "-fx-font-size: 20px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-background-radius: 15;"
                    + "-fx-border-radius: 15;"
                    + "-fx-border-color: white;"
                    + "-fx-border-width: 2;"
                    + "-fx-cursor: hand;";

    private static final String HOVER_BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to bottom right, #2a5298, #1e3c72);"
                    + "-fx-text-fill: yellow;"
                    + "-fx-font-size: 22px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-background-radius: 15;"
                    + "-fx-border-radius: 15;"
                    + "-fx-border-color: white;"
                    + "-fx-border-width: 2;"
                    + "-fx-cursor: hand;";

    public GamePauseOverlay(double canvasWidth,
                            double canvasHeight,
                            Runnable onResume,
                            Runnable onGameSaves,
                            Runnable onNewGame,
                            Runnable onQuit) {
        super(30);
        setAlignment(Pos.CENTER);
        setStyle(OVERLAY_STYLE);
        setPrefSize(canvasWidth * 0.8, canvasHeight * 0.6);

        Text pausedText = new Text("PAUSED");
        pausedText.setFont(Font.font("Arial Black", 60));
        pausedText.setFill(Color.WHITE);
        pausedText.setStroke(Color.LIGHTBLUE);
        pausedText.setStrokeWidth(2);

        Button resumeBtn = createStyledButton("Resume", onResume);
        Button gameSavesBtn = createStyledButton("Game Saves", onGameSaves);
        Button newGameBtn = createStyledButton("New Game", onNewGame);
        Button quitBtn = createStyledButton("Quit", onQuit);

        getChildren().addAll(pausedText, resumeBtn, gameSavesBtn, newGameBtn, quitBtn);
    }

    private Button createStyledButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setStyle(NORMAL_BUTTON_STYLE);
        button.setOnMouseEntered(e -> button.setStyle(HOVER_BUTTON_STYLE));
        button.setOnMouseExited(e -> button.setStyle(NORMAL_BUTTON_STYLE));
        if (action != null) {
            button.setOnAction(e -> action.run());
        }
        return button;
    }
}
