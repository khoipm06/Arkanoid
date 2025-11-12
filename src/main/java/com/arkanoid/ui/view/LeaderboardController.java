package com.arkanoid.ui.view;

import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.systems.sound.SoundManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Controller for leaderboard view
 * Displays all users sorted by high score (descending)
 */
public class LeaderboardController {
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private ListView<PlayerProfileManager.LeaderboardEntry> leaderboardList;

    @FXML
    public void initialize() {
        setupCellFactory();
        refreshData();
    }

    /**
     * Refresh leaderboard data from database
     * Called when scene is shown
     */
    public void refreshData() {
        // Query all users from database, sorted by high_score DESC
        List<PlayerProfileManager.LeaderboardEntry> leaderboardData = PlayerProfileManager.getLeaderboardData();
        ObservableList<PlayerProfileManager.LeaderboardEntry> items = FXCollections
                .observableArrayList(leaderboardData);
        leaderboardList.setItems(items);
    }

    /**
     * Setup custom cell factory for leaderboard entries
     */
    private void setupCellFactory() {
        leaderboardList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PlayerProfileManager.LeaderboardEntry item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    Label rank = new Label("#" + (getIndex() + 1));
                    rank.setMinWidth(50);
                    rank.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                    rank.setTextFill(Color.CYAN);

                    Label username = new Label(item.username);
                    username.setFont(Font.font("Arial", 16));
                    username.setTextFill(Color.WHITE);

                    HBox.setHgrow(username, Priority.ALWAYS);

                    Label score = new Label(String.valueOf(item.highScore));
                    score.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                    score.setTextFill(Color.YELLOW);

                    hBox.getChildren().addAll(rank, username, score);
                    setGraphic(hBox);
                }
            }
        });
    }

    @FXML
    private void onBackClick() {
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }
}
