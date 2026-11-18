package com.arkanoid.ui.view;

import com.arkanoid.systems.twoplayer.TwoPlayerMatchManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Game Over screen for two-player mode, scaled through {@link ViewportScaler}
 * and mounted into {@link SceneManager}'s single persistent {@code Scene}.
 */
public class TwoPlayerGameOverScreen {
    private static final double DESIGN_WIDTH = 800.0;
    private static final double DESIGN_HEIGHT = 600.0;

    private final Stage stage;
    private final int winningPlayer;
    private final TwoPlayerMatchManager.EndReason endReason;
    private final int player1Score;
    private final int player2Score;

    public TwoPlayerGameOverScreen(Stage stage, int winningPlayer,
                                   TwoPlayerMatchManager.EndReason endReason,
                                   int player1Score, int player2Score) {
        this.stage = stage != null ? stage : SceneManager.getStage();
        this.winningPlayer = winningPlayer;
        this.endReason = endReason;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
    }

    public void show() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1a1a2e;");

        Label titleLabel = new Label("GAME OVER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.web("#e94560"));

        Label resultLabel = new Label(getResultMessage());
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        resultLabel.setTextFill(winningPlayer == 0 ? Color.web("#f39c12") : Color.web("#2ecc71"));
        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(600);
        resultLabel.setAlignment(Pos.CENTER);

        Label scoreLabel = new Label(String.format(
                "Final Scores\n\nPlayer 1: %,d\nPlayer 2: %,d",
                player1Score, player2Score));
        scoreLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        scoreLabel.setTextFill(Color.web("#16213e"));
        scoreLabel.setStyle("-fx-background-color: #eaeaea; -fx-padding: 20; -fx-background-radius: 10;");
        scoreLabel.setAlignment(Pos.CENTER);

        String menuNormal = "-fx-background-color: #0f3460; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 5;";
        String menuHover = "-fx-background-color: #16213e; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 5;";
        Button menuButton = new Button("Return to Menu");
        menuButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        menuButton.setStyle(menuNormal);
        menuButton.setOnMouseEntered(e -> menuButton.setStyle(menuHover));
        menuButton.setOnMouseExited(e -> menuButton.setStyle(menuNormal));
        menuButton.setOnAction(e -> returnToMenu());

        String rematchNormal = "-fx-background-color: #e94560; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 5;";
        String rematchHover = "-fx-background-color: #c23544; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 5;";
        Button rematchButton = new Button("Rematch");
        rematchButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        rematchButton.setStyle(rematchNormal);
        rematchButton.setOnMouseEntered(e -> rematchButton.setStyle(rematchHover));
        rematchButton.setOnMouseExited(e -> rematchButton.setStyle(rematchNormal));
        rematchButton.setOnAction(e -> startRematch());

        layout.getChildren().addAll(titleLabel, resultLabel, scoreLabel, rematchButton, menuButton);

        StackPane scaledRoot = ViewportScaler.wrap(layout, DESIGN_WIDTH, DESIGN_HEIGHT);
        SceneManager.showCustomRoot("twoPlayerGameOver", scaledRoot, new SceneLifecycle() {});
    }

    private String getResultMessage() {
        switch (endReason) {
            case SCORE_REACHED:
                return "Player " + winningPlayer + " Wins!\nReached 10,000 Points!";
            case LIVES_DEPLETED:
                return "Player " + winningPlayer + " Wins!\nOpponent Lost All Lives";
            case BRICKS_CLEARED:
                if (winningPlayer == 0) {
                    return "Draw!\nAll Bricks Cleared with Equal Scores";
                } else {
                    return "Player " + winningPlayer + " Wins!\nAll Bricks Cleared";
                }
            case DRAW:
                return "Draw!";
            default:
                return "Match Ended";
        }
    }

    private void returnToMenu() {
        SceneManager.switchTo("mainMenuView");
    }

    private void startRematch() {
        TwoPlayerGameScreen gameScreen = new TwoPlayerGameScreen(stage != null ? stage : SceneManager.getStage());
        gameScreen.show();
    }
}
