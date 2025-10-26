package com.arkanoid.systems.sound;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static MediaPlayer backgroundMusic;
    private static final Map<String, AudioClip> soundEffects = new HashMap<>();

    public static void playBackground(String fileName, boolean loop) {
        stopBackground();
        URL resource = SoundManager.class.getResource("/sounds/" + fileName);
        if (resource != null) {
            Media media = new Media(resource.toString());
            backgroundMusic = new MediaPlayer(media);
            backgroundMusic.setVolume(0.6);
            if (loop) backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusic.play();
        } else {
            System.out.println("Could not find music file: " + fileName);
        }
    }

    public static void stopBackground() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
    }

    public static void playSound(String fileName) {
        AudioClip clip = soundEffects.computeIfAbsent(fileName, key -> {
            URL resource = SoundManager.class.getResource("/sounds/" + key);
            return resource != null ? new AudioClip(resource.toString()) : null;
        });

        if (clip != null) {
            clip.play();
        } else {
            System.out.println("Could not find sound effect: " + fileName);
        }
    }
}
