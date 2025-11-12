package com.arkanoid.systems.player;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Paddle;

public class Player {
    private PlayerProfile profile;
    private PlayerState state;
    private Paddle paddle;
    private Ball ball;
    private int playerNumber;
    private Orientation orientation;

    public Player(String playerId, int playerNumber, Paddle paddle) {
        this.profile = new PlayerProfile(playerId);
        this.state = new PlayerState();
        this.paddle = paddle;
        this.playerNumber = playerNumber;
        this.orientation = Orientation.fromPlayerNumber(playerNumber);
    }

    public Ball getBall() {
        return ball;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
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

    public Orientation getOrientation() {
        return orientation;
    }

    public void update(double deltaTime) {
        paddle.update(deltaTime);
        if (ball != null) {
            if (ball.isAttachedToPaddle()) {
                ball.setX(paddle.getCenterX() - ball.getRadius());
                double attachYOffset = (orientation == Orientation.BOTTOM) ? -ball.getHeight() : paddle.getHeight();
                ball.setY(paddle.getY() + attachYOffset);
            }
            ball.update(deltaTime);
            // Check collision with own paddle
            ball.checkPaddleCollision(paddle);
        }
    }
}
