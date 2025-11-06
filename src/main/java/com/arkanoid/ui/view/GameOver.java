package com.arkanoid.ui.view;

import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class GameOver {
    @FXML
    private Label score;
    @FXML private Label highestScore;

    private int currentLevel;
    private int currentScore;
    private GameScene gameScene;
    private Stage stage;
    private GameManager gameManager;

    public void init(int level, int scoreValue, int highest) {
        this.currentLevel = level;
        this.currentScore = scoreValue;
        score.setText("Score: " + scoreValue);
        highestScore.setText("Highest Score: " + highest);
        this.stage = (Stage) score.getScene().getWindow();
        SoundManager.stopBackground();
        SoundManager.playSound("GameOver.wav");
    }

    @FXML
    private void onBackHomeClick() {
        SoundManager.playSound("Accept.wav");
        SoundManager.playBackground("background.mp3", true);
        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    private void onNewGameClick() {
        SoundManager.playSound("Accept.wav");
        SoundManager.playBackground("background.mp3", true);
        if (stage == null) {
            stage = (Stage) score.getScene().getWindow();
        }

        GameScene newScene = new GameScene(stage, stage.getWidth(), stage.getHeight(), currentLevel);
        stage.setScene(newScene.getScene());
        stage.show();
        newScene.start();
    }
}
