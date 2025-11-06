package com.arkanoid.core.entities;

import javafx.scene.paint.Color;

public class UnbreakableBrick extends BaseBrick {
    public UnbreakableBrick(double x, double y, double width, double height, int row, int col, String path) {
        super(x, y, width, height, Integer.MAX_VALUE, Color.GRAY, row , col, path);
        this.powerUpChance = 0;
    }

    @Override
    public void hit() {
    }
}
