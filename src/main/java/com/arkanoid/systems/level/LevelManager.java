package com.arkanoid.systems.level;

import com.arkanoid.core.entities.Brick;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
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
        try {
            String levelFile = "/levels/level" + levelNumber + ".json";
            InputStream is = getClass().getResourceAsStream(levelFile);
            
            if (is == null) {
                return createDefaultLevel();
            }

            Gson gson = new Gson();
            JsonObject levelData = gson.fromJson(new InputStreamReader(is), JsonObject.class);
            
            return parseLevelData(levelData);
        } catch (Exception e) {
            return createDefaultLevel();
        }
    }

    private List<Brick> parseLevelData(JsonObject levelData) {
        List<Brick> bricks = new ArrayList<>();
        JsonArray bricksArray = levelData.getAsJsonArray("bricks");

        for (int i = 0; i < bricksArray.size(); i++) {
            JsonObject brickData = bricksArray.get(i).getAsJsonObject();
            Brick brick = entityFactory.createBrick(brickData);
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
                if (row == 0) type = "strong";
                if (row == 2 && col % 3 == 0) type = "moving";
                
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
                
                bricks.add(entityFactory.createBrick(brickData));
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
}
