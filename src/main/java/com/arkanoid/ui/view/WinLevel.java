package com.arkanoid.ui.view;

import com.arkanoid.GameApplication;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;


public class WinLevel {
    @FXML
    private Label score;
    @FXML
    private Label timePlayed;
    @FXML
    private Button nextLevel;
    @FXML
    private Button preLevel;
    @FXML
    private Button replay;
    @FXML
    private ImageView winImage;

    private int currentLevel;
    private int currentScore;
    private final int maxLevel = 3;

    public void init(int level, int scoreValue, String timePlayedS) {
        this.currentLevel = level;
        this.currentScore = scoreValue;
        score.setText("Score: " + scoreValue);
        timePlayed.setText("Time: " + timePlayedS);
        if (currentLevel >= maxLevel) {
            nextLevel.setDisable(true);
            nextLevel.setOpacity(0.5);
        }
        SoundManager.stopBackground();
        SoundManager.playSound("win.wav");
        try {
            Image img = new Image(getClass().getResource("/images/You_Win.png").toExternalForm());
            winImage.setImage(img);
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh win.png: " + e.getMessage());
        }
        winImage.setScaleX(0.1);
        winImage.setScaleY(0.1);
        ScaleTransition zoom = new ScaleTransition(Duration.seconds(0.8), winImage);
        zoom.setToX(1.0);
        zoom.setToY(1.0);
        zoom.setInterpolator(Interpolator.EASE_OUT);
        zoom.play();
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
    private void onHomeClick() {
        SoundManager.playSound("Accept.wav");
        SoundManager.playBackground("background.mp3", true);
        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    private void onReplayClick() {
        SoundManager.playSound("Accept.wav");
        SoundManager.playBackground("background.mp3", true);

        Stage stage = (Stage) replay.getScene().getWindow();

        GameScene newScene = new GameScene(stage, stage.getWidth(), stage.getHeight(), currentLevel);
        stage.setScene(newScene.getScene());
        stage.show();
        newScene.start();

    }
}
