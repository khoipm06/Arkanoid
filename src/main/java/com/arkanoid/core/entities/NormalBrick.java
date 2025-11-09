package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class NormalBrick extends BaseBrick {
    public NormalBrick(double x, double y, double width, double height, int col, int row, String path) {
        super(x, y, width, height, 1, Color.ORANGE, row, col, path);
    }
}
