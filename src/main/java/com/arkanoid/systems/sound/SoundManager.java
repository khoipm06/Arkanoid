package com.arkanoid.systems.sound;

import com.arkanoid.systems.logging.GameLogger;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe manager for background music and cached sound effects.
 */
public class SoundManager {
    private static final Logger logger = GameLogger.getLogger(SoundManager.class);
    private static volatile SoundManager instance;

    private MediaPlayer backgroundMusic;
    private final Map<String, AudioClip> soundEffects = new ConcurrentHashMap<>();
    private volatile double currentVolume = 0.6;

    private SoundManager() {
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            synchronized (SoundManager.class) {
                if (instance == null) {
                    instance = new SoundManager();
                }
            }
        }
        return instance;
    }

    public synchronized void setVolume(double volume) {
        currentVolume = Math.max(0.0, Math.min(1.0, volume));
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

    public synchronized void playBackground(String fileName, boolean loop) {
        stopBackground();
        URL resource = SoundManager.class.getResource("/sounds/" + fileName);
        if (resource == null) {
            logger.warn("Could not find music file: {}", fileName);
            return;
        }
        try {
            Media media = new Media(resource.toString());
            backgroundMusic = new MediaPlayer(media);
            backgroundMusic.setVolume(currentVolume);
            if (loop) {
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            }
            backgroundMusic.play();
        } catch (Exception e) {
            logger.warn("Failed to play background music '{}': {}", fileName, e.getMessage());
            backgroundMusic = null;
        }
    }

    public synchronized void stopBackground() {
        if (backgroundMusic != null) {
            try {
                backgroundMusic.stop();
                backgroundMusic.dispose();
            } catch (Exception e) {
                logger.debug("Error disposing background music: {}", e.getMessage());
            } finally {
                backgroundMusic = null;
            }
        }
    }

    public void playSound(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        AudioClip clip = soundEffects.computeIfAbsent(fileName, key -> {
            URL resource = SoundManager.class.getResource("/sounds/" + key);
            if (resource == null) {
                return null;
            }
            try {
                AudioClip created = new AudioClip(resource.toString());
                created.setVolume(currentVolume);
                return created;
            } catch (Exception e) {
                logger.debug("Could not initialize AudioClip '{}': {}", key, e.getMessage());
                return null;
            }
        });

        if (clip != null) {
            clip.play();
        } else {
            logger.warn("Could not find or load sound effect: {}", fileName);
        }
    }

    /**
     * Preloads multiple sound effects asynchronously without blocking background worker threads.
     *
     * @param soundFileNames Array of sound file names to preload
     * @return CompletableFuture that completes when all sounds have finished loading or failed cleanly
     */
    public CompletableFuture<Void> preloadSoundsAsync(String... soundFileNames) {
        if (soundFileNames == null || soundFileNames.length == 0) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        for (String fileName : soundFileNames) {
            if (fileName == null || soundEffects.containsKey(fileName)) {
                continue;
            }
            URL resource = SoundManager.class.getResource("/sounds/" + fileName);
            if (resource == null) {
                logger.warn("Could not find sound file for preload: {}", fileName);
                continue;
            }

            CompletableFuture<Void> itemFuture = new CompletableFuture<>();
            tasks.add(itemFuture);
            try {
                Platform.runLater(() -> {
                    try {
                        soundEffects.computeIfAbsent(fileName, k -> {
                            AudioClip clip = new AudioClip(resource.toString());
                            clip.setVolume(currentVolume);
                            return clip;
                        });
                        logger.debug("Preloaded sound: {}", fileName);
                    } catch (Exception e) {
                        logger.warn("Failed to preload sound '{}': {}", fileName, e.getMessage());
                    } finally {
                        itemFuture.complete(null);
                    }
                });
            } catch (IllegalStateException e) {
                logger.debug("JavaFX toolkit not initialized, skipping async sound preload for '{}'", fileName);
                itemFuture.complete(null);
            }
        }

        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }

    /**
     * Preloads all common game sound effects asynchronously.
     */
    public CompletableFuture<Void> preloadGameSoundsAsync() {
        return preloadSoundsAsync(
                "background.mp3",
                "Accept.wav",
                "brickBounce.wav",
                "paddleBounce.wav",
                "GameOver.wav",
                "win.wav"
        );
    }

    /**
     * Stops background music and clears cached sound effects. Thread-safe and idempotent.
     */
    public synchronized void cleanup() {
        stopBackground();
        soundEffects.clear();
        logger.info("SoundManager cleaned up");
    }
}
