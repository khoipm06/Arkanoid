package com.arkanoid.ui.view;

import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.systems.sound.SoundManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class LeaderboardController {

    @FXML
    private ListView<PlayerProfileManager.LeaderboardEntry> leaderboardList;

    @FXML
    public void initialize() {
        List<PlayerProfileManager.LeaderboardEntry> leaderboardData = PlayerProfileManager.getLeaderboardData();
        ObservableList<PlayerProfileManager.LeaderboardEntry> items = FXCollections.observableArrayList(leaderboardData);
        leaderboardList.setItems(items);

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

                    Tooltip tooltip = new Tooltip("Reached Level: " + item.levelReached);
                    setTooltip(tooltip);
                }
            }
        });
    }

    @FXML
    private void onBackClick() {
        SoundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }
}
