package com.arkanoid.systems.player;

public class PlayerState {
    private int score;
    private int lives;
    private int level;
    private double skillEnergy;
    private final double maxSkillEnergy;

    public PlayerState() {
        this.score = 0;
        this.lives = 3;
        this.level = 1;
        this.skillEnergy = 0;
        this.maxSkillEnergy = 100;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        lives--;
    }

    public void addLife() {
        lives++;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    public int getLevel() {
        return level;
    }

    public void nextLevel() {
        level++;
    }

    public double getSkillEnergy() {
        return skillEnergy;
    }

    public void addSkillEnergy(double amount) {
        skillEnergy = Math.min(maxSkillEnergy, skillEnergy + amount);
    }

    public boolean useSkill(double cost) {
        if (skillEnergy >= cost) {
            skillEnergy -= cost;
            return true;
        }
        return false;
    }

    public void reset() {
        score = 0;
        lives = 3;
        level = 1;
        skillEnergy = 0;
    }
}
