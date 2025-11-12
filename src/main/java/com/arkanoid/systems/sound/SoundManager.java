package com.arkanoid.systems.sound;

import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.threading.ThreadManager;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Preloads multiple sound effects asynchronously on a background thread.
     * Useful for preloading all game sounds during initialization to avoid latency during gameplay.
     * 
     * @param soundFileNames Array of sound file names to preload (e.g., "Brick.wav", "Paddle.wav")
     * @return CompletableFuture that completes when all sounds are loaded
     */
    public CompletableFuture<Void> preloadSoundsAsync(String... soundFileNames) {
        if (soundFileNames == null || soundFileNames.length == 0) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        
        ThreadManager.getInstance().executeBackground(() -> {
            try {
                int loaded = 0;
                int failed = 0;
                
                for (String fileName : soundFileNames) {
                    if (!soundEffects.containsKey(fileName)) {
                        URL resource = SoundManager.class.getResource("/sounds/" + fileName);
                        if (resource != null) {
                            // AudioClip creation must happen on JavaFX thread
                            CompletableFuture<AudioClip> clipFuture = new CompletableFuture<>();
                            Platform.runLater(() -> {
                                try {
                                    AudioClip clip = new AudioClip(resource.toString());
                                    clip.setVolume(currentVolume);
                                    clipFuture.complete(clip);
                                } catch (Exception e) {
                                    clipFuture.completeExceptionally(e);
                                }
                            });
                            
                            // Wait for clip creation
                            AudioClip clip = clipFuture.join();
                            soundEffects.put(fileName, clip);
                            loaded++;
                            logger.debug("Preloaded sound: {}", fileName);
                        } else {
                            logger.warn("Could not find sound file for preload: {}", fileName);
                            failed++;
                        }
                    }
                }
                
                logger.info("Sound preload complete: {} loaded, {} failed, {} already cached", 
                        loaded, failed, soundFileNames.length - loaded - failed);
                future.complete(null);
                
            } catch (Exception e) {
                logger.error("Failed to preload sounds: {}", e.getMessage(), e);
                future.completeExceptionally(e);
            }
        }, "PreloadSounds");
        
        return future;
    }

    /**
     * Preloads all common game sound effects asynchronously.
     * 
     * @return CompletableFuture that completes when all sounds are loaded
     */
    public CompletableFuture<Void> preloadGameSoundsAsync() {
        return preloadSoundsAsync(
            "Brick.wav",
            "Paddle.wav",
            "Wall.wav",
            "PowerUp.wav",
            "LoseLife.wav",
            "Accept.wav"
        );
    }
}
