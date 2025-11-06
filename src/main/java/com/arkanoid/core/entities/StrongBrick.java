package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class StrongBrick extends BaseBrick {
    public StrongBrick(double x, double y, double width, double height, int row, int col, String path) {
        super(x, y, width, height, 3, Color.PURPLE, row, col, path);
    }
}
