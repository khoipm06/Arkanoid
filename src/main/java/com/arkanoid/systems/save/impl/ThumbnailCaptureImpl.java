package com.arkanoid.systems.save.impl;

import com.arkanoid.systems.save.ThumbnailCapture;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of ThumbnailCapture using JavaFX WritableImage and ImageIO.
 */
public class ThumbnailCaptureImpl implements ThumbnailCapture {

    /**
     * Captures a thumbnail from the game canvas using default dimensions (200x150).
     * 
     * @param canvas The game canvas to capture
     * @return PNG-encoded byte array
     * @throws IOException if capture or encoding fails
     */
    @Override
    public byte[] captureThumbnailPNG(WritableImage canvas) throws IOException {
        return captureThumbnailPNG(canvas, DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT);
    }

    /**
     * Captures a thumbnail from the game canvas with custom dimensions.
     * Maintains aspect ratio and validates size does not exceed
     * MAX_THUMBNAIL_SIZE_BYTES (100KB).
     * 
     * @param canvas       The game canvas to capture
     * @param targetWidth  Target width in pixels (1-1000)
     * @param targetHeight Target height in pixels (1-1000)
     * @return PNG-encoded byte array
     * @throws IllegalArgumentException if canvas or dimensions invalid
     * @throws IOException              if capture or encoding fails
     * @throws IllegalStateException    if thumbnail exceeds size limit
     */
    @Override
    public byte[] captureThumbnailPNG(WritableImage canvas, int targetWidth, int targetHeight) throws IOException {
        if (!isValidCanvas(canvas)) {
            throw new IllegalArgumentException("Canvas is null or has invalid dimensions");
        }

        if (targetWidth <= 0 || targetWidth > 1000 || targetHeight <= 0 || targetHeight > 1000) {
            throw new IllegalArgumentException("Invalid target dimensions: " + targetWidth + "x" + targetHeight);
        }

        try {
            // Calculate dimensions maintaining aspect ratio
            ThumbnailDimensions dimensions = calculateThumbnailDimensions(
                    canvas.getWidth(),
                    canvas.getHeight());

            // Create scaled image
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(canvas, null);
            BufferedImage scaledImage = scaleImage(bufferedImage, dimensions.width(), dimensions.height());

            // Encode to PNG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(scaledImage, "png", baos);
            byte[] pngBytes = baos.toByteArray();

            // Validate size
            if (pngBytes.length > MAX_THUMBNAIL_SIZE_BYTES) {
                throw new IllegalStateException(
                        "Thumbnail exceeds maximum size: " + pngBytes.length + " bytes");
            }

            return pngBytes;
        } catch (IOException e) {
            throw new IOException("Failed to encode thumbnail to PNG", e);
        }
    }

    /**
     * Loads a thumbnail image from PNG byte array.
     * 
     * @param pngBytes PNG-encoded byte array
     * @return JavaFX Image object
     * @throws IllegalArgumentException if pngBytes is null or invalid
     * @throws IOException              if decoding fails
     */
    @Override
    public Image loadThumbnailFromBytes(byte[] pngBytes) throws IOException {
        if (!isValidPngBytes(pngBytes)) {
            throw new IllegalArgumentException("Invalid PNG bytes");
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(pngBytes);
            return new Image(bais);
        } catch (Exception e) {
            throw new IOException("Failed to decode thumbnail from PNG bytes", e);
        }
    }

    @Override
    public boolean isValidCanvas(WritableImage canvas) {
        return canvas != null && canvas.getWidth() > 0 && canvas.getHeight() > 0;
    }

    @Override
    public boolean isValidPngBytes(byte[] pngBytes) {
        if (pngBytes == null || pngBytes.length == 0 || pngBytes.length > MAX_THUMBNAIL_SIZE_BYTES) {
            return false;
        }

        // Check PNG header (89 50 4E 47 0D 0A 1A 0A)
        if (pngBytes.length < 8) {
            return false;
        }

        return pngBytes[0] == (byte) 0x89 &&
                pngBytes[1] == (byte) 0x50 &&
                pngBytes[2] == (byte) 0x4E &&
                pngBytes[3] == (byte) 0x47;
    }

    @Override
    public ThumbnailDimensions calculateThumbnailDimensions(double canvasWidth, double canvasHeight) {
        double aspectRatio = canvasWidth / canvasHeight;

        int width, height;
        if (aspectRatio > (double) DEFAULT_THUMBNAIL_WIDTH / DEFAULT_THUMBNAIL_HEIGHT) {
            // Canvas is wider - fit to width
            width = DEFAULT_THUMBNAIL_WIDTH;
            height = (int) (DEFAULT_THUMBNAIL_WIDTH / aspectRatio);
        } else {
            // Canvas is taller - fit to height
            height = DEFAULT_THUMBNAIL_HEIGHT;
            width = (int) (DEFAULT_THUMBNAIL_HEIGHT * aspectRatio);
        }

        return new ThumbnailDimensions(width, height);
    }

    @Override
    public int estimateThumbnailSizeBytes(int width, int height) {
        // Rough estimation: RGBA (4 bytes per pixel) * 0.5 compression ratio
        return (int) (width * height * 4 * 0.5);
    }

    /**
     * Scales a BufferedImage to the specified dimensions.
     */
    private BufferedImage scaleImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = scaled.createGraphics();

        // Use high-quality scaling
        g.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return scaled;
    }
}
