package com.arkanoid.database.entity;

import java.time.LocalDateTime;

/**
 * Entity representing a saved game state with LZ4-compressed game state
 */
public class GameSave {
    private final int id;
    private final int userId;
    private final String saveName;
    private final int levelNumber;
    private final int score;
    private final int lives;
    private final int elapsedTimeSeconds;
    private final byte[] compressedGameState; // LZ4-compressed JSON
    private final byte[] thumbnailData;
    private final LocalDateTime createdAt;

    public GameSave(int id, int userId, String saveName, int levelNumber, int score, int lives,
            int elapsedTimeSeconds, byte[] compressedGameState, byte[] thumbnailData, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.saveName = saveName;
        this.levelNumber = levelNumber;
        this.score = score;
        this.lives = lives;
        this.elapsedTimeSeconds = elapsedTimeSeconds;
        this.compressedGameState = compressedGameState;
        this.thumbnailData = thumbnailData;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getSaveName() {
        return saveName;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getElapsedTimeSeconds() {
        return elapsedTimeSeconds;
    }

    public byte[] getCompressedGameState() {
        return compressedGameState;
    }

    public byte[] getThumbnailData() {
        return thumbnailData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
