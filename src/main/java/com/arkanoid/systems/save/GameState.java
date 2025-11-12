package com.arkanoid.systems.save;

import java.util.List;

/**
 * Data Transfer Object representing the complete game state for serialization.
 * This class captures all necessary information to save and restore a game
 * session.
 */
public class GameState {
    private int levelNumber;
    private int score;
    private int lives;
    private int elapsedTimeSeconds;
    private PaddleState paddleState;
    private List<BallState> ballStates;
    private List<BrickState> brickStates;
    private List<PowerUpState> activePowerUps;

    public GameState() {
    }

    public GameState(int levelNumber, int score, int lives, int elapsedTimeSeconds,
                     PaddleState paddleState, List<BallState> ballStates,
                     List<BrickState> brickStates, List<PowerUpState> activePowerUps) {
        this.levelNumber = levelNumber;
        this.score = score;
        this.lives = lives;
        this.elapsedTimeSeconds = elapsedTimeSeconds;
        this.paddleState = paddleState;
        this.ballStates = ballStates;
        this.brickStates = brickStates;
        this.activePowerUps = activePowerUps;
    }


    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getElapsedTimeSeconds() {
        return elapsedTimeSeconds;
    }

    public void setElapsedTimeSeconds(int elapsedTimeSeconds) {
        this.elapsedTimeSeconds = elapsedTimeSeconds;
    }

    public PaddleState getPaddleState() {
        return paddleState;
    }

    public void setPaddleState(PaddleState paddleState) {
        this.paddleState = paddleState;
    }

    public List<BallState> getBallStates() {
        return ballStates;
    }

    public void setBallStates(List<BallState> ballStates) {
        this.ballStates = ballStates;
    }

    public List<BrickState> getBrickStates() {
        return brickStates;
    }

    public void setBrickStates(List<BrickState> brickStates) {
        this.brickStates = brickStates;
    }

    public List<PowerUpState> getActivePowerUps() {
        return activePowerUps;
    }

    public void setActivePowerUps(List<PowerUpState> activePowerUps) {
        this.activePowerUps = activePowerUps;
    }
}
