package com.arkanoid.systems.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerProfile {
    private static PlayerProfile currentPlayer;
    private final String playerId;
    private final Map<String, Integer> inventory;
    private String currentSkin;
    private String currentPaddleSkin = "paddle_Default";

    public PlayerProfile(String playerId) {
        this.playerId = playerId;
        this.inventory = new HashMap<>();
        this.currentSkin = "Default";
        this.currentPaddleSkin = "paddle_Default";
        PlayerProfile.setCurrentPlayer(this);
    }

    public static PlayerProfile getCurrentPlayer() {
        return currentPlayer;
    }

    public static void setCurrentPlayer(PlayerProfile player) {
        currentPlayer = player;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void addItem(String itemId, int quantity) {
        inventory.put(itemId, inventory.getOrDefault(itemId, 0) + quantity);
    }

    public boolean hasItem(String itemId) {
        return inventory.getOrDefault(itemId, 0) > 0;
    }

    public String getCurrentSkin() {
        return currentSkin;
    }

    public void setCurrentSkin(String skin) {
        this.currentSkin = skin;
    }

    public String getEquippedPaddleSkin() {
        return currentPaddleSkin;
    }

    public void setEquippedPaddleSkin(String skin) {
        this.currentPaddleSkin = skin;
    }
}
