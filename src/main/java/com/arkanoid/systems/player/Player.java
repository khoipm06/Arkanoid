package com.arkanoid.systems.player;

import com.arkanoid.core.entities.Paddle;

public class Player {
    private PlayerProfile profile;
    private PlayerState state;
    private Paddle paddle;
    private int playerNumber;

    public Player(String playerId, int playerNumber, Paddle paddle) {
        this.profile = new PlayerProfile(playerId);
        this.state = new PlayerState();
        this.paddle = paddle;
        this.playerNumber = playerNumber;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public PlayerState getState() {
        return state;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void update(double deltaTime) {
        paddle.update(deltaTime);
    }
}
