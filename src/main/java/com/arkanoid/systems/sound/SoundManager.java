package com.arkanoid.systems.sound;

import com.arkanoid.systems.logging.GameLogger;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Logger logger = GameLogger.getLogger(SoundManager.class);
    private static SoundManager instance;
    private MediaPlayer backgroundMusic;
    private final Map<String, AudioClip> soundEffects = new HashMap<>();
    private double currentVolume = 0.6;

    private SoundManager() {
    }

    // Thread-safe singleton getInstance
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void setVolume(double volume) {
        currentVolume = volume;
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(currentVolume);
        }
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(currentVolume);
        }
    }

    public double getVolume() {
        return currentVolume;
    }

    public void playBackground(String fileName, boolean loop) {
        stopBackground();
        URL resource = SoundManager.class.getResource("/sounds/" + fileName);
        if (resource != null) {
            Media media = new Media(resource.toString());
            backgroundMusic = new MediaPlayer(media);
            backgroundMusic.setVolume(currentVolume);
            if (loop)
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.play();
        } else {
            logger.warn("Could not find music file: {}", fileName);
        }
    }

    public void stopBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
    }

    public void playSound(String fileName) {
        AudioClip clip = soundEffects.computeIfAbsent(fileName, key -> {
            URL resource = SoundManager.class.getResource("/sounds/" + key);
            return resource != null ? new AudioClip(resource.toString()) : null;
        });

        if (clip != null) {
            clip.play();
        } else {
            logger.warn("Could not find sound effect: {}", fileName);
        }
    }
}
