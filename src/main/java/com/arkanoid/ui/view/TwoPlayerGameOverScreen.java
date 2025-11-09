package com.arkanoid.ui.view;

import com.arkanoid.systems.twoplayer.TwoPlayerMatchManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Game Over screen for two-player mode.
 * Displays match results, winner/draw status, and final scores.
 */
public class TwoPlayerGameOverScreen {

    private final Stage stage;
    private final int winningPlayer;
    private final TwoPlayerMatchManager.EndReason endReason;
    private final int player1Score;
    private final int player2Score;

    public TwoPlayerGameOverScreen(Stage stage, int winningPlayer,
            TwoPlayerMatchManager.EndReason endReason,
            int player1Score, int player2Score) {
        this.stage = stage;
        this.winningPlayer = winningPlayer;
        this.endReason = endReason;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
    }

    public void show() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1a1a2e;");

        // Title
        Label titleLabel = new Label("GAME OVER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.web("#e94560"));

        // Winner/Draw message
        Label resultLabel = new Label(getResultMessage());
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        resultLabel.setTextFill(winningPlayer == 0 ? Color.web("#f39c12") : Color.web("#2ecc71"));
        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(600);
        resultLabel.setAlignment(Pos.CENTER);

        // Score display
        Label scoreLabel = new Label(String.format(
                "Final Scores\n\nPlayer 1: %,d\nPlayer 2: %,d",
                player1Score, player2Score));
        scoreLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        scoreLabel.setTextFill(Color.web("#16213e"));
        scoreLabel.setStyle("-fx-background-color: #eaeaea; -fx-padding: 20; -fx-background-radius: 10;");
        scoreLabel.setAlignment(Pos.CENTER);

        // Return to menu button
        Button menuButton = new Button("Return to Menu");
        menuButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        menuButton.setStyle(
                "-fx-background-color: #0f3460; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 5;");
        menuButton.setOnMouseEntered(e -> menuButton.setStyle(
                "-fx-background-color: #16213e; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 5;"));
        menuButton.setOnMouseExited(e -> menuButton.setStyle(
                "-fx-background-color: #0f3460; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 5;"));
        menuButton.setOnAction(e -> returnToMenu());

        // Rematch button
        Button rematchButton = new Button("Rematch");
        rematchButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        rematchButton.setStyle(
                "-fx-background-color: #e94560; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 5;");
        rematchButton.setOnMouseEntered(e -> rematchButton.setStyle(
                "-fx-background-color: #c23544; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 5;"));
        rematchButton.setOnMouseExited(e -> rematchButton.setStyle(
                "-fx-background-color: #e94560; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 5;"));
        rematchButton.setOnAction(e -> startRematch());

        layout.getChildren().addAll(titleLabel, resultLabel, scoreLabel, rematchButton, menuButton);

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Game Over - Two Player Mode");
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
        TwoPlayerGameScreen gameScreen = new TwoPlayerGameScreen(stage);
        gameScreen.show();
    }
}
