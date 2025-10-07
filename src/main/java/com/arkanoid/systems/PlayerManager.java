package com.arkanoid.systems;

import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.player.Player;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private Map<Integer, Player> players;
    private Map<Integer, KeyCode> leftKeys;
    private Map<Integer, KeyCode> rightKeys;

    public PlayerManager() {
        this.players = new HashMap<>();
        this.leftKeys = new HashMap<>();
        this.rightKeys = new HashMap<>();
        
        leftKeys.put(1, KeyCode.LEFT);
        rightKeys.put(1, KeyCode.RIGHT);
        leftKeys.put(2, KeyCode.A);
        rightKeys.put(2, KeyCode.D);
    }

    public void addPlayer(int playerNumber, Player player) {
        players.put(playerNumber, player);
    }

    public Player getPlayer(int playerNumber) {
        return players.get(playerNumber);
    }

    public void handleInput(KeyCode key, boolean pressed, double deltaTime) {
        for (Map.Entry<Integer, Player> entry : players.entrySet()) {
            int playerNum = entry.getKey();
            Player player = entry.getValue();
            Paddle paddle = player.getPaddle();

            if (pressed) {
                if (key == leftKeys.get(playerNum)) {
                    paddle.moveLeft(deltaTime);
                } else if (key == rightKeys.get(playerNum)) {
                    paddle.moveRight(deltaTime);
                }
            } else {
                if (key == leftKeys.get(playerNum) || key == rightKeys.get(playerNum)) {
                    paddle.stop();
                }
            }
        }
    }

    public void update(double deltaTime) {
        for (Player player : players.values()) {
            player.update(deltaTime);
        }
    }
}
