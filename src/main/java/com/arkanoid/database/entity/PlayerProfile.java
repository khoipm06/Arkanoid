package com.arkanoid.database.entity;

/**
 * Entity representing a player's profile data
 */
public class PlayerProfile {
    private final int userId;
    private int money;
    private int highScore;
    private String currentSkin;
    private int gamesPlayed;
    private int totalScore;

    public PlayerProfile(int userId, int money, int highScore, String currentSkin, int gamesPlayed, int totalScore) {
        this.userId = userId;
        this.money = money;
        this.highScore = highScore;
        this.currentSkin = currentSkin;
        this.gamesPlayed = gamesPlayed;
        this.totalScore = totalScore;
    }

    public int getUserId() {
        return userId;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        if (highScore > this.highScore) {
            this.highScore = highScore;
        }
    }

    public String getCurrentSkin() {
        return currentSkin;
    }

    public void setCurrentSkin(String currentSkin) {
        this.currentSkin = currentSkin;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void addToTotalScore(int score) {
        this.totalScore += score;
    }
}
