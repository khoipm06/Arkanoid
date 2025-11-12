package com.arkanoid.systems.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerProfile {
    private String playerId;
    private int coins;
    private Map<String, Integer> inventory;
    private String currentSkin;
    private static PlayerProfile currentPlayer;

    private String currentPaddleSkin = "paddle_Default";;

    public PlayerProfile(String playerId) {
        this.playerId = playerId;
        this.coins = 0;
        this.inventory = new HashMap<>();
        this.currentSkin = "default";
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

    public static PlayerProfile getCurrentPlayer() {
        return currentPlayer;
    }

    public static void setCurrentPlayer(PlayerProfile player) {
        currentPlayer = player;
    }

    public String getEquippedPaddleSkin() {
        return currentPaddleSkin;
    }

    public void setEquippedPaddleSkin(String skin) {
        this.currentPaddleSkin = skin;
    }
}
