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
public class SettingView implements SceneLifecycle {
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
    private boolean listenerAttached = false;
    private boolean syncing = false;

    @FXML
    public void initialize() {
        setupVolumeListener();
        syncFromSession();
    }

    @Override
    public void onEnter() {
        syncFromSession();
    }

    /**
     * Synchronizes slider and audio volume from the active user session without
     * re-attaching change listeners.
     */
    public void syncFromSession() {
        syncing = true;
        try {
            loadUserPreferences();
        } finally {
            syncing = false;
        }
    }

    private void loadUserPreferences() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user == null) {
            currentVolume = 50;
        } else {
            Optional<UserPreferences> prefs = UserPreferencesManager.getPreferences(user.getId());
            currentVolume = prefs.map(UserPreferences::getMusicVolume).orElse(50);
        }

        if (volumeSlider != null) {
            volumeSlider.setValue(currentVolume);
        }
        if (valueLabel != null) {
            valueLabel.setText(currentVolume + "%");
        }
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
        if (listenerAttached || volumeSlider == null) {
            return;
        }
        listenerAttached = true;
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (syncing) {
                return;
            }
            int vol = newVal.intValue();
            if (valueLabel != null) {
                valueLabel.setText(vol + "%");
            }
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
        syncing = true;
        try {
            volumeSlider.setValue(currentVolume);
            valueLabel.setText(currentVolume + "%");
            soundManager.setVolume(currentVolume / 100.0);
        } finally {
            syncing = false;
        }

        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }
}
