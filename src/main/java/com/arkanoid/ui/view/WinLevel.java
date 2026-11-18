package com.arkanoid.ui.view;

import com.arkanoid.core.GameConstants;
import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import com.arkanoid.utils.ImageLoader;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    public void init(int level, int scoreValue, String timePlayedText) {
        this.currentLevel = level;
        score.setText("Score: " + scoreValue);
        timePlayed.setText("Time: " + timePlayedText);

        saveHighScore(scoreValue);

        boolean atMaxLevel = currentLevel >= GameConstants.MAX_LEVEL;
        nextLevel.setDisable(atMaxLevel);
        nextLevel.setOpacity(atMaxLevel ? 0.5 : 1.0);

        soundManager.stopBackground();
        soundManager.playSound("win.wav");

        Image img = ImageLoader.loadImage("/images/You_Win.png");
        if (img != null) {
            winImage.setImage(img);
        } else {
            logger.error("Win screen initialization failed: Unable to load You_Win.png image resource");
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
            PlayerProfileManager.updateHighScore(user.getId(), scoreValue);
            PlayerProfileManager.incrementGamesPlayed(user.getId());
            PlayerProfileManager.addToTotalScore(user.getId(), scoreValue);
            logger.info("Saved game stats for user: {} | Score: {}", user.getUsername(), scoreValue);
        }
    }

    @FXML
    private void onNextLevelClick() {
        soundManager.playSound("Accept.wav");

        if (currentLevel < GameConstants.MAX_LEVEL) {
            int next = currentLevel + 1;
            soundManager.playBackground("background.mp3", true);
            startLevel(next);
        } else {
            logger.debug("Already at the last level");
        }
    }

    @FXML
    private void onPreLevelClick() {
        soundManager.playSound("Accept.wav");
        int prev = Math.max(1, currentLevel - 1);
        soundManager.playBackground("background.mp3", true);
        startLevel(prev);
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
        startLevel(currentLevel);
    }

    private void startLevel(int level) {
        GameScene gameScene = GameScene.getInstance(
                SceneManager.getStage(),
                GameConstants.WINDOW_WIDTH,
                GameConstants.WINDOW_HEIGHT,
                level
        );
        gameScene.start();
    }
}
