package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class NormalBrick extends BaseBrick {
    public NormalBrick(double x, double y, double width, double height, int row, int col) {
        super(x, y, width, height, 1, Color.ORANGE, row, col);
    }
}
