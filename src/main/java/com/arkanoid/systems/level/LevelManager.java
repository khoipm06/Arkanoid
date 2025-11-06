package com.arkanoid.systems.level;

import com.arkanoid.core.entities.Brick;
import com.arkanoid.core.entities.NormalBrick;
import com.google.gson.*;
import org.json.JSONArray;
import org.json.JSONObject;

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
                double brickWidth = brickData.get("width").getAsDouble(); // Assuming brickWidth is available in data or fixed
                double brickHeight = brickData.get("height").getAsDouble(); // Assuming brickHeight is available in data or fixed
                double startX = 50; // Based on createDefaultLevel
                double startY = 50; // Based on createDefaultLevel
                double gap = 5; // Based on createDefaultLevel

                int row = (int) ((y - startY) / (brickHeight + gap));
                int col = (int) ((x - startX) / (brickWidth + gap));

                Brick brick = entityFactory.createBrick(brickData, row, col);
                if (brick != null) bricks.add(brick);
            }
        } catch (Exception e) {
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
        try (InputStream input = getClass().getResourceAsStream(mapPath)) {
            if (input == null) {
                System.err.println("Không tìm thấy file map: " + mapPath);
                return bricks;
            }

            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject data = new JSONObject(json);
            JSONArray layout = data.getJSONArray("layout");

            for (int row = 0; row < layout.length(); row++) {
                String line = layout.getString(row);
                for (int col = 0; col < line.length(); col++) {
                    if (line.charAt(col) == '1') {
                        bricks.add(new NormalBrick(col * 40, row * 20, 40, 20, row, col));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bricks;
    }


}
