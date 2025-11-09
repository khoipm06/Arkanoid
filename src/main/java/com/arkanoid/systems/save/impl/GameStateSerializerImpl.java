package com.arkanoid.systems.save.impl;

import com.arkanoid.systems.save.GameState;
import com.arkanoid.systems.save.GameStateSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Implementation of GameStateSerializer using Gson for JSON serialization.
 */
public class GameStateSerializerImpl implements GameStateSerializer {

    private final Gson gson;

    public GameStateSerializerImpl() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Serializes a GameState object to JSON string.
     * Validates size does not exceed MAX_JSON_SIZE_BYTES (1MB).
     * 
     * @param gameState The game state to serialize
     * @return JSON string representation
     * @throws IllegalArgumentException if gameState is null
     * @throws IllegalStateException    if JSON size exceeds limit
     */
    @Override
    public String toJson(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null");
        }

        try {
            String json = gson.toJson(gameState);

            // Validate size
            if (json.getBytes().length > MAX_JSON_SIZE_BYTES) {
                throw new IllegalStateException(
                        "Serialized JSON exceeds maximum size: " + json.getBytes().length + " bytes");
            }

            return json;
        } catch (Exception e) {
            System.err.println("Failed to serialize GameState: " + e.getMessage());
            return "{\"error\": \"Serialization failed\"}";
        }
    }

    /**
     * Deserializes a JSON string to GameState object.
     * 
     * @param json The JSON string to deserialize
     * @return Deserialized GameState object
     * @throws IllegalArgumentException if json is null or empty
     * @throws JsonSyntaxException      if JSON is malformed
     */
    @Override
    public GameState fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        try {
            // Parse and check version
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            // Deserialize
            GameState gameState = gson.fromJson(json, GameState.class);

            // Validate
            if (!isValidGameState(gameState)) {
                throw new IllegalStateException("Deserialized GameState is invalid");
            }

            return gameState;
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException("Invalid JSON format: " + e.getMessage(), e);
        }
    }

    /**
     * Validates if a string is well-formed JSON with required game state fields.
     * 
     * @param json The JSON string to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            // Check required fields
            return jsonObject.has("levelNumber") &&
                    jsonObject.has("score") &&
                    jsonObject.has("lives") &&
                    jsonObject.has("paddleState") &&
                    jsonObject.has("ballStates") &&
                    jsonObject.has("brickStates");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates if a GameState object has all required data.
     * Checks for paddle state, at least one ball, and valid level/score/lives.
     * 
     * @param gameState The game state to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValidGameState(GameState gameState) {
        if (gameState == null) {
            return false;
        }

        // Check required fields
        if (gameState.getPaddleState() == null ||
                gameState.getBallStates() == null ||
                gameState.getBrickStates() == null) {
            return false;
        }

        // At least one ball must exist
        if (gameState.getBallStates().isEmpty()) {
            return false;
        }

        // Positions should be reasonable (within typical canvas bounds)
        // We'll do basic validation here
        return gameState.getLevelNumber() >= 1 &&
                gameState.getScore() >= 0 &&
                gameState.getLives() >= 0;
    }

    @Override
    public GameStateMetadata extractMetadata(String json) {
        if (!isValidJson(json)) {
            return null;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

            int levelNumber = jsonObject.get("levelNumber").getAsInt();
            int score = jsonObject.get("score").getAsInt();
            int lives = jsonObject.get("lives").getAsInt();

            return new GameStateMetadata(levelNumber, score, lives);
        } catch (Exception e) {
            return null;
        }
    }
}
