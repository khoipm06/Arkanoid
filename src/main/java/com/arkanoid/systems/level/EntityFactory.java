package com.arkanoid.systems.level;

import com.arkanoid.core.entities.*;
import com.google.gson.JsonObject;

public class EntityFactory {
    
    public Brick createBrick(JsonObject data) {
        String type = data.get("type").getAsString();
        double x = data.get("x").getAsDouble();
        double y = data.get("y").getAsDouble();
        double width = data.get("width").getAsDouble();
        double height = data.get("height").getAsDouble();

        return switch (type.toLowerCase()) {
            case "normal" -> new NormalBrick(x, y, width, height);
            case "strong" -> new StrongBrick(x, y, width, height);
            case "unbreakable" -> new UnbreakableBrick(x, y, width, height);
            case "moving" -> {
                double minX = data.has("minX") ? data.get("minX").getAsDouble() : 0;
                double maxX = data.has("maxX") ? data.get("maxX").getAsDouble() : 800;
                yield new MovingBrick(x, y, width, height, minX, maxX);
            }
            default -> new NormalBrick(x, y, width, height);
        };
    }

    public PowerUp createPowerUp(String type, double x, double y) {
        return switch (type.toLowerCase()) {
            case "expand" -> new ExpandPaddlePowerUp(x, y);
            case "multiball" -> new MultiBallPowerUp(x, y);
            case "explosive" -> new ExplosiveBallPowerUp(x, y);
            case "gun" -> new GunPaddlePowerUp(x, y);
            default -> null;
        };
    }
}
