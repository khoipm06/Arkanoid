package com.arkanoid.systems.level;

import com.arkanoid.core.entities.Brick;
import com.arkanoid.systems.logging.GameLogger;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private static final Logger logger = GameLogger.getLogger(LevelManager.class);
    private EntityFactory entityFactory;
    private int currentLevel;

    public LevelManager() {
        this.entityFactory = new EntityFactory();
        this.currentLevel = 1;
    }

    public List<Brick> loadLevel(int levelNumber) {
        List<Brick> bricks = new ArrayList<>();
        boolean multiplayer = levelNumber <= 0;
        String levelPath = multiplayer ? "/levels/level_multiplayer.json" : "/levels/level" + levelNumber + ".json";

        try (InputStream inputStream = getClass().getResourceAsStream(levelPath)) {
            if (inputStream == null) {
                if (multiplayer) {
                    logger.error("Could not find level_multiplayer.json");
                } else {
                    logger.error("Could not find {}", levelPath);
                }
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

            if (multiplayer) {
                logger.info("Loaded {} bricks for multiplayer level", bricks.size());
            } else {
                logger.info("Loaded {} bricks for level {}", bricks.size(), levelPath);
            }
        } catch (Exception e) {
            if (multiplayer) {
                logger.error("Error loading multiplayer level", e);
            } else {
                logger.error("Error loading level {}", levelPath, e);
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
