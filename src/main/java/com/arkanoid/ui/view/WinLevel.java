package com.arkanoid.ui.view;

import com.arkanoid.GameApplication;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public class WinLevel {
    @FXML
    private Label score;
    @FXML private Label highestScore;
    @FXML private Button nextLevel;
    @FXML private Button preLevel;

    private int currentLevel;
    private int currentScore;
    private int maxLevel = 3;

    public void init(int level, int scoreValue, int highest) {
        this.currentLevel = level;
        this.currentScore = scoreValue;
        score.setText("Score: " + scoreValue);
        highestScore.setText("Highest Score: " + highest);
        if (currentLevel >= maxLevel) {
            nextLevel.setDisable(true);
            nextLevel.setOpacity(0.5);
        }
        SoundManager.stopBackground();
        SoundManager.playSound("win.wav");
    }

    @FXML
    private void onNextLevelClick() {
        SoundManager.playSound("Accept.wav");

        if (currentLevel < maxLevel) {
            int next = currentLevel + 1;
            SoundManager.playBackground("background.mp3", true);

            Stage stage = (Stage) nextLevel.getScene().getWindow();
            GameScene nextScene = new GameScene(stage, GameApplication.GAME_WIDTH, GameApplication.GAME_HEIGHT, next);

            stage.setScene(nextScene.getScene());
            stage.show();
            nextScene.start();
        } else {
            System.out.println("Bạn đã ở level cuối cùng!");
        }
    }

    @FXML
    private void onPreLevelClick() {
        SoundManager.playSound("Accept.wav");

        int prev = Math.max(1, currentLevel - 1);
        SoundManager.playBackground("background.mp3", true);

        Stage stage = (Stage) preLevel.getScene().getWindow();
        GameScene prevScene = new GameScene(stage, GameApplication.GAME_WIDTH, GameApplication.GAME_HEIGHT, prev);

        stage.setScene(prevScene.getScene());
        stage.show();
        prevScene.start();
    }

    @FXML
    private void onBackHomeClick() {
        SoundManager.playSound("Accept.wav");
        SoundManager.playBackground("background.mp3", true);
        SceneManager.switchTo("mainMenuView");
    }
}
