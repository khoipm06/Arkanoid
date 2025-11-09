package com.arkanoid.ui.view;

import com.arkanoid.systems.player.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Stats panel component for two-player mode.
 * Displays real-time score and lives for both players.
 */
public class TwoPlayerStatsPanel extends VBox {

    private final Player player1;
    private final Player player2;

    private Label player1ScoreLabel;
    private Label player1LivesLabel;
    private Label player2ScoreLabel;
    private Label player2LivesLabel;

    public TwoPlayerStatsPanel(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;

        setupUI();
    }

    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        setPrefWidth(200);

        // Player 1 Section
        VBox player1Section = createPlayerSection(1);

        // Divider
        Label divider = new Label("VS");
        divider.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        divider.setTextFill(Color.web("#e94560"));
        divider.setAlignment(Pos.CENTER);

        // Player 2 Section
        VBox player2Section = createPlayerSection(2);

        getChildren().addAll(player1Section, divider, player2Section);
    }

    private VBox createPlayerSection(int playerNumber) {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        section.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 5; -fx-padding: 15;");

        // Player header
        Label headerLabel = new Label("Player " + playerNumber);
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        headerLabel.setTextFill(Color.web("#eaeaea"));

        // Score label
        Label scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        scoreLabel.setTextFill(Color.web("#2ecc71"));

        // Lives label
        Label livesLabel = new Label("Lives: 3");
        livesLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        livesLabel.setTextFill(Color.web("#f39c12"));

        section.getChildren().addAll(headerLabel, scoreLabel, livesLabel);

        // Store references for updates
        if (playerNumber == 1) {
            player1ScoreLabel = scoreLabel;
            player1LivesLabel = livesLabel;
        } else {
            player2ScoreLabel = scoreLabel;
            player2LivesLabel = livesLabel;
        }

        return section;
    }

    /**
     * Updates the stats panel with current player states.
     * Should be called each frame or on state changes.
     */
    public void update() {
        // Update Player 1
        player1ScoreLabel.setText("Score: " + String.format("%,d", player1.getState().getScore()));
        player1LivesLabel.setText("Lives: " + player1.getState().getLives());

        // Update Player 2
        player2ScoreLabel.setText("Score: " + String.format("%,d", player2.getState().getScore()));
        player2LivesLabel.setText("Lives: " + player2.getState().getLives());
    }
}
