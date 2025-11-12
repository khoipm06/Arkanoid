package com.arkanoid.ui.view;

import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;

public class GameOver {
    private static final Logger logger = GameLogger.getLogger(GameOver.class);
    @FXML
    private Label score;
    @FXML
    private Label timePlayed;
    @FXML
    private ImageView youLostImage;

    private int currentLevel;
    private int currentScore;
    // private GameScene gameScene;
    private Stage stage;
    // private GameManager gameManager;
    private static final SoundManager soundManager = SoundManager.getInstance();

    public void init(int level, int scoreValue, String timePlyed) {
        this.currentLevel = level;
        this.currentScore = scoreValue;
        score.setText("Score: " + scoreValue);
        timePlayed.setText("Time:" + timePlyed);

        // Save high score to database if user is logged in
        saveHighScore(scoreValue);

        this.stage = (Stage) score.getScene().getWindow();
        soundManager.stopBackground();
        soundManager.playSound("GameOver.wav");
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

        SequentialTransition sequence = new SequentialTransition(fall, bounce);
        sequence.play();
    }

    private void saveHighScore(int scoreValue) {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
            PlayerProfileManager.updateHighScore(user.getId(), scoreValue);
            PlayerProfileManager.incrementGamesPlayed(user.getId());
            PlayerProfileManager.addToTotalScore(user.getId(), scoreValue);
            logger.info("Saved game stats for user: {}", user.getUsername());
        }
    }

    @FXML
    private void onBackHomeClick() {
        soundManager.playSound("Accept.wav");
        soundManager.playBackground("background.mp3", true);
        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    private void onNewGameClick() {
        soundManager.playSound("Accept.wav");
        soundManager.playBackground("background.mp3", true);
        if (stage == null) {
            stage = (Stage) score.getScene().getWindow();
        }

        GameScene newScene = new GameScene(stage, stage.getWidth(), stage.getHeight(), currentLevel);
        stage.setScene(newScene.getScene());
        stage.show();
        newScene.start();
    }
}
