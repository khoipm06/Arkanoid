package com.arkanoid.systems;

import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.player.Player;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager instance;
    private final Map<Integer, Player> players;
    private final Map<Integer, KeyCode> leftKeys;
    private final Map<Integer, KeyCode> rightKeys;

    private PlayerManager() {
        this.players = new HashMap<>();
        this.leftKeys = new HashMap<>();
        this.rightKeys = new HashMap<>();

        leftKeys.put(1, KeyCode.LEFT);
        rightKeys.put(1, KeyCode.RIGHT);
        leftKeys.put(2, KeyCode.A);
        rightKeys.put(2, KeyCode.D);
    }

    // Thread-safe singleton getInstance
    public static synchronized PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
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
