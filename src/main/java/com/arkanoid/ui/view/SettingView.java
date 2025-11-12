package com.arkanoid.ui.view;

import com.arkanoid.database.UserPreferencesManager;
import com.arkanoid.database.entity.UserPreferences;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Settings view for adjusting music volume. Volume preference is saved per-user
 * in the database.
 */
public class SettingView {
    private static final Logger logger = GameLogger.getLogger(SettingView.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Slider volumeSlider;
    @FXML
    private Label valueLabel;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    private int currentVolume = 50;

    @FXML
    public void initialize() {
        loadUserPreferences();
        setupVolumeListener();
    }

    private void loadUserPreferences() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user == null) {
            // Guest mode - use default
            currentVolume = 50;
        } else {
            Optional<UserPreferences> prefs = UserPreferencesManager.getPreferences(user.getId());
            currentVolume = prefs.map(UserPreferences::getMusicVolume).orElse(50);
        }

        volumeSlider.setValue(currentVolume);
        valueLabel.setText(currentVolume + "%");
        soundManager.setVolume(currentVolume / 100.0);
    }

    private void savePreferences() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
            int volume = (int) volumeSlider.getValue();
            UserPreferencesManager.updateMusicVolume(user.getId(), volume);
            logger.info("Saved music volume for user {}: {}", user.getUsername(), volume);
        }
    }

    private void setupVolumeListener() {
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int vol = newVal.intValue();
            valueLabel.setText(vol + "%");
            soundManager.setVolume(vol / 100.0);
        });
    }

    @FXML
    private void onOKClick() {
        savePreferences();
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    private void onCancelClick() {
        volumeSlider.setValue(currentVolume);
        soundManager.setVolume(currentVolume / 100.0);

        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }
}
