package com.arkanoid.core.entities;

import com.arkanoid.utils.ImageLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class StrongBrick extends BaseBrick {
    private final Image[] crackImages;
    private int hitCount = 0;
    private boolean destroyed = false;

    public StrongBrick(double x, double y, double width, double height, int row, int col, String path) {
        super(x, y, width, height, 3, Color.PURPLE, row, col, path);
        crackImages = new Image[3];
        if (path != null) {
            crackImages[0] = ImageLoader.loadImage(path);
            crackImages[1] = ImageLoader.loadImage(path.replace(".png", "break1.png"));
            crackImages[2] = ImageLoader.loadImage(path.replace(".png", "break2.png"));
        }

        for (int i = 1; i < crackImages.length; i++) {
            if (crackImages[i] == null) {
                crackImages[i] = crackImages[0];
            }
        }
    }

    @Override
    public void hit() {
        if (isDestroyed())
            return;

        hitCount++;
        super.hit();
        if (hitCount >= 3) {
            destroy();
        }
    }

    @Override
    public void destroy() {
        this.destroyed = true;
        this.active = false;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (isDestroyed())
            return;

        int state = Math.min(hitCount, 2);
        Image img = crackImages[state];
        if (img != null) {
            gc.drawImage(img, getX(), getY(), getWidth(), getHeight());
        } else {
            gc.setFill(Color.PURPLE);
            gc.fillRect(getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public void instantDestroy() {
        super.instantDestroy();
        this.hitCount = 3;
        this.destroyed = true;
    }
}
