package com.arkanoid.core.components;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderer {
    private GraphicsContext gc;

    public Renderer(GraphicsContext gc) {
        this.gc = gc;
    }

    public void clear(double width, double height) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
    }

    public void drawRect(double x, double y, double width, double height, Color color) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
    }

    public void drawCircle(double x, double y, double radius, Color color) {
        gc.setFill(color);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    public void drawText(String text, double x, double y, Color color, double fontSize) {
        gc.setFill(color);
        gc.setFont(javafx.scene.text.Font.font(fontSize));
        gc.fillText(text, x, y);
    }

    public GraphicsContext getGraphicsContext() {
        return gc;
    }
}
