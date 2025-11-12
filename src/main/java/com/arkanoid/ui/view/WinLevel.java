package com.arkanoid.ui.view;

import com.arkanoid.GameApplication;
import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.systems.logging.GameLogger;
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
import org.slf4j.Logger;

public class WinLevel {
    private static final Logger logger = GameLogger.getLogger(WinLevel.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

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
        
        // Save high score to database if user is logged in
        saveHighScore(scoreValue);
        
        if (currentLevel >= maxLevel) {
            nextLevel.setDisable(true);
            nextLevel.setOpacity(0.5);
        }
        soundManager.stopBackground();
        soundManager.playSound("win.wav");
        try {
            Image img = new Image(getClass().getResource("/images/You_Win.png").toExternalForm());
            winImage.setImage(img);
        } catch (Exception e) {
            logger.error("Could not find image win.png: {}", e.getMessage());
        }
        winImage.setScaleX(0.1);
        winImage.setScaleY(0.1);
        ScaleTransition zoom = new ScaleTransition(Duration.seconds(0.8), winImage);
        zoom.setToX(1.0);
        zoom.setToY(1.0);
        zoom.setInterpolator(Interpolator.EASE_OUT);
        zoom.play();
    }

    private void saveHighScore(int scoreValue) {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
            // Update high score if current score is higher
            PlayerProfileManager.updateHighScore(user.getId(), scoreValue);
            // Increment games played
            PlayerProfileManager.incrementGamesPlayed(user.getId());
            // Add to total score
            PlayerProfileManager.addToTotalScore(user.getId(), scoreValue);
            logger.info("Saved game stats for user: {} | Score: {}", user.getUsername(), scoreValue);
        }
    }

    @FXML
    private void onNextLevelClick() {
        soundManager.playSound("Accept.wav");

        if (currentLevel < maxLevel) {
            int next = currentLevel + 1;
            soundManager.playBackground("background.mp3", true);

            Stage stage = (Stage) nextLevel.getScene().getWindow();
            GameScene nextScene = GameScene.getInstance(stage, GameApplication.GAME_WIDTH, GameApplication.GAME_HEIGHT, next);

            stage.setScene(nextScene.getScene());
            stage.show();
            nextScene.start();
        } else {
            logger.debug("Already at the last level");
        }
    }

    @FXML
    private void onPreLevelClick() {
        soundManager.playSound("Accept.wav");

        int prev = Math.max(1, currentLevel - 1);
        soundManager.playBackground("background.mp3", true);

        Stage stage = (Stage) preLevel.getScene().getWindow();
        GameScene prevScene = GameScene.getInstance(stage, GameApplication.GAME_WIDTH, GameApplication.GAME_HEIGHT, prev);

        stage.setScene(prevScene.getScene());
        stage.show();
        prevScene.start();
    }

    @FXML
    private void onHomeClick() {
        soundManager.playSound("Accept.wav");
        soundManager.playBackground("background.mp3", true);
        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    private void onReplayClick() {
        soundManager.playSound("Accept.wav");
        soundManager.playBackground("background.mp3", true);

        Stage stage = (Stage) replay.getScene().getWindow();

        GameScene newScene = GameScene.getInstance(stage, stage.getWidth(), stage.getHeight(), currentLevel);
        stage.setScene(newScene.getScene());
        stage.show();
        newScene.start();
    }
}
