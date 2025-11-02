package com.arkanoid.core.components;

import javafx.scene.image.Image;

public class Sprite {
    private Image image;
    private double width;
    private double height;

    public Sprite(String imagePath, double width, double height) {
        this.width = width;
        this.height = height;
        try {
            this.image = new Image(imagePath);
        } catch (Exception e) {
            this.image = null;
        }
    }

    public Image getImage() {
        return image;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean isLoaded() {
        return image != null;
    }
}
