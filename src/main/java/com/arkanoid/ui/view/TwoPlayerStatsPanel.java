package com.arkanoid.ui.view;

import com.arkanoid.core.GameConstants;
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
 * Displays real-time score and lives for both players with dirty-checked label updates.
 */
public class TwoPlayerStatsPanel extends VBox {

    private Player player1;
    private Player player2;

    private Label player1ScoreLabel;
    private Label player1LivesLabel;
    private Label player2ScoreLabel;
    private Label player2LivesLabel;

    private int lastP1Score = Integer.MIN_VALUE;
    private int lastP1Lives = Integer.MIN_VALUE;
    private int lastP2Score = Integer.MIN_VALUE;
    private int lastP2Lives = Integer.MIN_VALUE;

    public TwoPlayerStatsPanel(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;

        setupUI();
        update();
    }

    /**
     * Rebinds the stats panel to new {@link Player} instances (for example, on match restart).
     */
    public void bindPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.lastP1Score = Integer.MIN_VALUE;
        this.lastP1Lives = Integer.MIN_VALUE;
        this.lastP2Score = Integer.MIN_VALUE;
        this.lastP2Lives = Integer.MIN_VALUE;
        update();
    }

    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");
        setMinWidth(GameConstants.TWO_PLAYER_PANEL_WIDTH);
        setPrefWidth(GameConstants.TWO_PLAYER_PANEL_WIDTH);
        setMaxWidth(GameConstants.TWO_PLAYER_PANEL_WIDTH);

        VBox player1Section = createPlayerSection(1);

        Label divider = new Label("VS");
        divider.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        divider.setTextFill(Color.web("#e94560"));
        divider.setAlignment(Pos.CENTER);

        VBox player2Section = createPlayerSection(2);

        getChildren().addAll(player1Section, divider, player2Section);
    }

    private VBox createPlayerSection(int playerNumber) {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        section.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 5; -fx-padding: 15;");

        Label headerLabel = new Label("Player " + playerNumber);
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        headerLabel.setTextFill(Color.web("#eaeaea"));

        Label scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        scoreLabel.setTextFill(Color.web("#2ecc71"));

        Label livesLabel = new Label("Lives: 3");
        livesLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        livesLabel.setTextFill(Color.web("#f39c12"));

        section.getChildren().addAll(headerLabel, scoreLabel, livesLabel);

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
     * Updates the stats panel only when player score or lives values actually change.
     */
    public void update() {
        if (player1 != null && player1.getState() != null) {
            int p1Score = player1.getState().getScore();
            int p1Lives = player1.getState().getLives();
            if (p1Score != lastP1Score) {
                lastP1Score = p1Score;
                player1ScoreLabel.setText("Score: " + String.format("%,d", p1Score));
            }
            if (p1Lives != lastP1Lives) {
                lastP1Lives = p1Lives;
                player1LivesLabel.setText("Lives: " + p1Lives);
            }
        }

        if (player2 != null && player2.getState() != null) {
            int p2Score = player2.getState().getScore();
            int p2Lives = player2.getState().getLives();
            if (p2Score != lastP2Score) {
                lastP2Score = p2Score;
                player2ScoreLabel.setText("Score: " + String.format("%,d", p2Score));
            }
            if (p2Lives != lastP2Lives) {
                lastP2Lives = p2Lives;
                player2LivesLabel.setText("Lives: " + p2Lives);
            }
        }
    }
}
