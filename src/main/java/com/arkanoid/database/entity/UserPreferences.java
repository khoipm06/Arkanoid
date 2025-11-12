package com.arkanoid.database.entity;

/**
 * Entity representing user-specific game preferences.
 * Currently only stores music volume preference.
 */
public class UserPreferences {
    private int userId;
    private int musicVolume;

    public UserPreferences() {
    }

    public UserPreferences(int userId) {
        this.userId = userId;
        this.musicVolume = 50;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = Math.max(0, Math.min(100, musicVolume));
    }

    /**
     * Convert music volume to 0.0-1.0 range for SoundManager
     */
    public double getMusicVolumeAsDouble() {
        return musicVolume / 100.0;
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "userId=" + userId +
                ", musicVolume=" + musicVolume +
                '}';
    }
}
