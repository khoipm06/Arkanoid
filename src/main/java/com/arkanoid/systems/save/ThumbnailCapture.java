package com.arkanoid.systems.save;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.IOException;

/**
 * Contract for capturing and processing game thumbnail images.
 * 
 * <p>
 * This interface handles the capture of in-game canvas snapshots and their
 * conversion to a format suitable for database storage and UI display.
 * 
 * <p>
 * <b>Implementation Requirements:</b>
 * <ul>
 * <li>Must use JavaFX WritableImage.snapshot() API</li>
 * <li>Output must be PNG format (lossless compression)</li>
 * <li>Target thumbnail size: 200x150 pixels (maintaining aspect ratio)</li>
 * <li>Maximum file size: 50KB (enforce compression/quality tradeoffs)</li>
 * <li>Must handle canvas size variations gracefully</li>
 * </ul>
 * 
 * <p>
 * <b>Performance:</b> Thumbnail capture should complete in &lt;200ms to avoid
 * blocking the save operation. Scaling and encoding can be done on background
 * thread.
 * 
 * <p>
 * <b>Thread Safety:</b> snapshot() must be called on JavaFX Application Thread,
 * but encoding can be done on background thread.
 * 
 * @author Game Save System Specification
 * @version 1.0
 * @see javafx.scene.image.WritableImage
 */
public interface ThumbnailCapture {

    /**
     * Default thumbnail width in pixels.
     */
    int DEFAULT_THUMBNAIL_WIDTH = 200;

    /**
     * Default thumbnail height in pixels.
     */
    int DEFAULT_THUMBNAIL_HEIGHT = 150;

    /**
     * Maximum allowed thumbnail size in bytes (50KB).
     */
    int MAX_THUMBNAIL_SIZE_BYTES = 51_200; // 50KB

    // ==================== Capture Operations ====================

    /**
     * Captures a snapshot of the game canvas and converts it to PNG bytes.
     * 
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Capture full-size canvas snapshot (must be on JavaFX thread)</li>
     * <li>Scale to thumbnail size (200x150) maintaining aspect ratio</li>
     * <li>Encode to PNG format using ImageIO or JavaFX API</li>
     * <li>Compress to ensure size &lt; 50KB</li>
     * <li>Return byte array for database storage</li>
     * </ol>
     * 
     * <p>
     * <b>Canvas Snapshot API:</b>
     * 
     * <pre>
     * WritableImage snapshot = new WritableImage(
     *         (int) canvas.getWidth(),
     *         (int) canvas.getHeight());
     * canvas.snapshot(new SnapshotParameters(), snapshot);
     * </pre>
     * 
     * <p>
     * <b>Scaling Strategy:</b> Use smooth scaling (Bilinear or Bicubic) to maintain
     * visual quality. Preserve aspect ratio by cropping if necessary.
     * 
     * <p>
     * <b>Performance:</b> Target &lt;200ms for typical 800x600 canvas.
     * 
     * @param canvas the game canvas to capture
     * @return PNG-encoded byte array (length &lt; 50KB)
     * @throws IllegalArgumentException if canvas is null or has zero dimensions
     * @throws IOException              if PNG encoding fails
     * @throws IllegalStateException    if thumbnail exceeds
     *                                  MAX_THUMBNAIL_SIZE_BYTES
     */
    byte[] captureThumbnailPNG(WritableImage canvas) throws IOException;

    /**
     * Captures a thumbnail with custom dimensions.
     * 
     * @param canvas       the game canvas to capture
     * @param targetWidth  desired thumbnail width
     * @param targetHeight desired thumbnail height
     * @return PNG-encoded byte array
     * @throws IllegalArgumentException if dimensions are invalid (&lt;= 0 or &gt;
     *                                  1000)
     * @throws IOException              if PNG encoding fails
     */
    byte[] captureThumbnailPNG(WritableImage canvas, int targetWidth, int targetHeight)
            throws IOException;

    // ==================== Load Operations ====================

    /**
     * Converts PNG byte array back to a JavaFX Image for UI display.
     * 
     * <p>
     * Used when loading thumbnails from database for ListView cells.
     * 
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Create ByteArrayInputStream from PNG bytes</li>
     * <li>Construct JavaFX Image from stream</li>
     * <li>Validate image loaded successfully (not error image)</li>
     * </ol>
     * 
     * <p>
     * <b>Performance:</b> Target &lt;50ms for typical 200x150 PNG.
     * Results should be cached to avoid repeated decoding.
     * 
     * @param pngBytes PNG-encoded image data from database
     * @return JavaFX Image ready for display
     * @throws IllegalArgumentException if pngBytes is null or empty
     * @throws IOException              if PNG decoding fails (corrupted data)
     */
    Image loadThumbnailFromBytes(byte[] pngBytes) throws IOException;

    // ==================== Validation ====================

    /**
     * Validates a canvas before attempting thumbnail capture.
     * 
     * <p>
     * Checks:
     * <ul>
     * <li>Canvas not null</li>
     * <li>Canvas has non-zero dimensions</li>
     * <li>Canvas width/height &gt; 0</li>
     * </ul>
     * 
     * @param canvas the canvas to validate
     * @return true if canvas can be captured, false otherwise
     */
    boolean isValidCanvas(WritableImage canvas);

    /**
     * Validates PNG byte array before attempting to load as image.
     * 
     * <p>
     * Checks:
     * <ul>
     * <li>Not null or empty</li>
     * <li>Starts with PNG header (89 50 4E 47)</li>
     * <li>Size within reasonable limits (0-50KB)</li>
     * </ul>
     * 
     * @param pngBytes the byte array to validate
     * @return true if appears to be valid PNG, false otherwise
     */
    boolean isValidPngBytes(byte[] pngBytes);

    // ==================== Utility ====================

    /**
     * Gets the recommended thumbnail dimensions for a given canvas size.
     * Maintains aspect ratio while fitting within max dimensions.
     * 
     * <p>
     * Example: 800x600 canvas → 200x150 thumbnail (4:3 ratio preserved)
     * 
     * @param canvasWidth  the source canvas width
     * @param canvasHeight the source canvas height
     * @return recommended thumbnail dimensions
     */
    ThumbnailDimensions calculateThumbnailDimensions(double canvasWidth, double canvasHeight);

    /**
     * Recommended thumbnail dimensions.
     * 
     * @param width  thumbnail width in pixels
     * @param height thumbnail height in pixels
     */
    record ThumbnailDimensions(int width, int height) {
    }

    /**
     * Estimates the file size of a PNG thumbnail before encoding.
     * Used to warn if thumbnail may exceed size limits.
     * 
     * <p>
     * Estimation formula (rough):
     * <code>width * height * 4 * 0.5</code> (assumes 50% PNG compression ratio)
     * 
     * @param width  thumbnail width
     * @param height thumbnail height
     * @return estimated size in bytes
     */
    int estimateThumbnailSizeBytes(int width, int height);
}
