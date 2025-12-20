package com.arkanoid.core.entities;

import com.arkanoid.systems.logging.GameLogger;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.slf4j.Logger;

public class Explosion extends GameObject {
    private static final Logger logger = GameLogger.getLogger(Explosion.class);
    private final double duration;
    private double age;
    private final int frameCount = 6; // number of explosion frames in the sprite sheet
    private int currentFrame = 0;
    private final double frameWidth;
    private final double frameHeight;
    private Image image;

    public Explosion(double x, double y, double frameWidth, double frameHeight, double duration) {
        super(x, y, 0, 0);
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.duration = duration;
        this.age = 0;
        var url = getClass().getResource("/images/Explosion.png");
        if (url != null) {
            this.image = new Image(url.toExternalForm());
        } else {
            logger.warn("Explosion image not found");
        }
    }

    @Override
    public void update(double deltaTime) {
        age += deltaTime;
        double progress = Math.min(age / duration, 1.0);
        currentFrame = Math.min((int) (progress * frameCount), frameCount - 1);

        // Deactivate explosion after its duration
        if (age >= duration) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (image == null)
            return;

        // Draw the current frame
        gc.drawImage(
                image,
                currentFrame * frameWidth, 0, frameWidth, frameHeight, // cut frame from sprite sheet
                x - frameWidth / 2, y - frameHeight / 2, frameWidth, frameHeight // draw at center (x,y)
        );
    }
}
