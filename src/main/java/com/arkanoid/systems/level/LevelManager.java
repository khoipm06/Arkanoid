package com.arkanoid.systems.level;

import com.arkanoid.core.entities.Brick;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private EntityFactory entityFactory;
    private int currentLevel;

    public LevelManager() {
        this.entityFactory = new EntityFactory();
        this.currentLevel = 1;
    }

    public List<Brick> loadLevel(int levelNumber) {
        List<Brick> bricks = new ArrayList<>();
        try (InputStream inputStream = getClass().getResourceAsStream("/levels/level" + levelNumber + ".json")) {
            if (inputStream == null) {
                System.out.println(" Không tìm thấy file level" + levelNumber);
                return bricks;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            JsonArray brickArray = json.getAsJsonArray("bricks");

            for (JsonElement element : brickArray) {
                JsonObject brickData = element.getAsJsonObject();
                double x = brickData.get("x").getAsDouble();
                double y = brickData.get("y").getAsDouble();
                double brickWidth = brickData.get("width").getAsDouble();
                double brickHeight = brickData.get("height").getAsDouble();
                double startX = 50;
                double startY = 50;
                double gap = 5;

                int row = (int) ((y - startY) / (brickHeight + gap));
                int col = (int) ((x - startX) / (brickWidth + gap));

                Brick brick = entityFactory.createBrick(brickData, row, col);
                if (brick != null)
                    bricks.add(brick);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bricks;
    }

    /**
     * Loads the special multiplayer level layout from level_multiplayer.json
     * 
     * @return List of bricks for two-player competitive mode
     */
    public List<Brick> loadMultiplayerLevel() {
        List<Brick> bricks = new ArrayList<>();
        try (InputStream inputStream = getClass().getResourceAsStream("/levels/level_multiplayer.json")) {
            if (inputStream == null) {
                System.err.println("ERROR: Could not find level_multiplayer.json");
                return bricks;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray brickArray = json.getAsJsonArray("bricks");

            for (JsonElement element : brickArray) {
                JsonObject brickData = element.getAsJsonObject();
                double x = brickData.get("x").getAsDouble();
                double y = brickData.get("y").getAsDouble();
                double brickWidth = brickData.get("width").getAsDouble();
                double brickHeight = brickData.get("height").getAsDouble();
                double startX = 50;
                double startY = 50;
                double gap = 5;

                int row = (int) ((y - startY) / (brickHeight + gap));
                int col = (int) ((x - startX) / (brickWidth + gap));

                Brick brick = entityFactory.createBrick(brickData, row, col);
                if (brick != null) {
                    bricks.add(brick);
                }
            }
            System.out.println("Loaded " + bricks.size() + " bricks for multiplayer level");
        } catch (Exception e) {
            System.err.println("ERROR loading multiplayer level: " + e.getMessage());
            e.printStackTrace();
        }
        return bricks;
    }

    private List<Brick> parseLevelData(JsonObject levelData) {
        List<Brick> bricks = new ArrayList<>();
        JsonArray bricksArray = levelData.getAsJsonArray("bricks");

        for (int i = 0; i < bricksArray.size(); i++) {
            JsonObject brickData = bricksArray.get(i).getAsJsonObject();
            double x = brickData.get("x").getAsDouble();
            double y = brickData.get("y").getAsDouble();
            double brickWidth = brickData.get("width").getAsDouble();
            double brickHeight = brickData.get("height").getAsDouble();
            double startX = 50;
            double startY = 50;
            double gap = 5;

            int row = (int) ((y - startY) / (brickHeight + gap));
            int col = (int) ((x - startX) / (brickWidth + gap));
            Brick brick = entityFactory.createBrick(brickData, row, col);
            if (brick != null) {
                bricks.add(brick);
            }
        }

        return bricks;
    }

    private List<Brick> createDefaultLevel() {
        List<Brick> bricks = new ArrayList<>();
        double brickWidth = 60;
        double brickHeight = 20;
        double startX = 50;
        double startY = 50;
        double gap = 5;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 10; col++) {
                double x = startX + col * (brickWidth + gap);
                double y = startY + row * (brickHeight + gap);

                String type = "normal";
                if (row == 0) {
                    type = "strong";
                }
                if (row == 2 && col % 3 == 0) {
                    type = "moving";
                }

                JsonObject brickData = new JsonObject();
                brickData.addProperty("type", type);
                brickData.addProperty("x", x);
                brickData.addProperty("y", y);
                brickData.addProperty("width", brickWidth);
                brickData.addProperty("height", brickHeight);

                if (type.equals("moving")) {
                    brickData.addProperty("minX", 0);
                    brickData.addProperty("maxX", 800);
                }

                bricks.add(entityFactory.createBrick(brickData, row, col));
            }
        }

        return bricks;
    }

    public void nextLevel() {
        currentLevel++;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public List<Brick> loadLevelFromFile(String mapPath) {
        List<Brick> bricks = new ArrayList<>();
        try (InputStream inputStream = getClass().getResourceAsStream(mapPath)) {
            if (inputStream == null) {
                System.err.println("Could not find level file: " + mapPath);
                return bricks;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonArray brickArray = json.getAsJsonArray("bricks");

            for (JsonElement element : brickArray) {
                JsonObject brickData = element.getAsJsonObject();
                double x = brickData.get("x").getAsDouble();
                double y = brickData.get("y").getAsDouble();
                double brickWidth = brickData.get("width").getAsDouble();
                double brickHeight = brickData.get("height").getAsDouble();

                // Row and col are not essential for loading, can be calculated differently if
                // needed.
                // Passing 0 for now.
                Brick brick = entityFactory.createBrick(brickData, 0, 0);
                if (brick != null)
                    bricks.add(brick);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bricks;
    }
}
