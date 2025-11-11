package com.arkanoid.ui.view;

import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;




public class GameOver {
    @FXML
    private Label score;
    @FXML private Label timePlayed;
    @FXML
    private ImageView youLostImage;

    private int currentLevel;
    private int currentScore;
    private GameScene gameScene;
    private Stage stage;
    private GameManager gameManager;

    public void init(int level, int scoreValue, String timePlyed) {
        this.currentLevel = level;
        this.currentScore = scoreValue;
        score.setText("Score: " + scoreValue);
        timePlayed.setText("Time :" + timePlyed);


        this.stage = (Stage) score.getScene().getWindow();
        SoundManager.stopBackground();
        SoundManager.playSound("GameOver.wav");
        youLostImage.setImage(new Image(getClass().getResource("/images/You_Lose.png").toExternalForm()));
        youLostImage.setTranslateY(-200);
        youLostImage.setOpacity(1);

        TranslateTransition fall = new TranslateTransition(Duration.seconds(0.9), youLostImage);
        fall.setFromY(-200);
        fall.setToY(0);
        fall.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition bounce = new ScaleTransition(Duration.seconds(0.25), youLostImage);
        bounce.setFromX(1.0);
        bounce.setFromY(1.0);
        bounce.setToX(1.06);
        bounce.setToY(0.94);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(2);

        // Chạy hiệu ứng nối tiếp nhau
        SequentialTransition sequence = new SequentialTransition(fall, bounce);
        sequence.play();
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
