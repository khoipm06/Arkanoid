package com.arkanoid.utils;

import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.threading.ThreadManager;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for loading and caching images asynchronously.
 * Images are loaded on background threads and cached for reuse.
 */
public class ImageLoader {
    private static final Logger logger = GameLogger.getLogger(ImageLoader.class);
    private static final Map<String, Image> imageCache = new HashMap<>();

    private ImageLoader() {
        // Utility class - prevent instantiation
    }

    /**
     * Loads an image synchronously from the classpath and caches it.
     * 
     * @param path The resource path (e.g., "/images/ball.png")
     * @return The loaded Image, or null if loading fails
     */
    public static Image loadImage(String path) {
        return imageCache.computeIfAbsent(path, key -> {
            try {
                InputStream is = ImageLoader.class.getResourceAsStream(key);
                if (is != null) {
                    Image img = new Image(is);
                    logger.debug("Loaded image: {}", key);
                    return img;
                } else {
                    logger.warn("Image not found: {}", key);
                    return null;
                }
            } catch (Exception e) {
                logger.error("Failed to load image: {}", key, e);
                return null;
            }
        });
    }

    /**
     * Loads an image asynchronously on a background thread.
     * Image creation happens on JavaFX thread, but I/O happens in background.
     * 
     * @param path The resource path (e.g., "/images/ball.png")
     * @return CompletableFuture that completes with the loaded Image
     */
    public static CompletableFuture<Image> loadImageAsync(String path) {
        // Return cached image immediately if available
        if (imageCache.containsKey(path)) {
            return CompletableFuture.completedFuture(imageCache.get(path));
        }

        CompletableFuture<Image> future = new CompletableFuture<>();
        
        ThreadManager.getInstance().executeBackground(() -> {
            try {
                InputStream is = ImageLoader.class.getResourceAsStream(path);
                if (is == null) {
                    logger.warn("Image not found: {}", path);
                    future.completeExceptionally(new IllegalArgumentException("Image not found: " + path));
                    return;
                }

                // Image loading must happen on JavaFX thread
                Platform.runLater(() -> {
                    try {
                        Image img = new Image(is);
                        imageCache.put(path, img);
                        logger.debug("Loaded image async: {}", path);
                        future.complete(img);
                    } catch (Exception e) {
                        logger.error("Failed to load image: {}", path, e);
                        future.completeExceptionally(e);
                    }
                });

            } catch (Exception e) {
                logger.error("Failed to load image: {}", path, e);
                future.completeExceptionally(e);
            }
        }, "LoadImage");
        
        return future;
    }

    /**
     * Preloads multiple images asynchronously on background threads.
     * 
     * @param paths Array of resource paths to preload
     * @return CompletableFuture that completes when all images are loaded
     */
    public static CompletableFuture<Void> preloadImagesAsync(String... paths) {
        if (paths == null || paths.length == 0) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<?>[] futures = new CompletableFuture[paths.length];
        for (int i = 0; i < paths.length; i++) {
            futures[i] = loadImageAsync(paths[i]);
        }

        return CompletableFuture.allOf(futures)
            .thenAccept(v -> logger.info("Preloaded {} images", paths.length))
            .exceptionally(ex -> {
                logger.error("Failed to preload some images: {}", ex.getMessage());
                return null;
            });
    }

    /**
     * Preloads all common game images asynchronously.
     * 
     * @return CompletableFuture that completes when all images are loaded
     */
    public static CompletableFuture<Void> preloadGameImagesAsync() {
        return preloadImagesAsync(
            "/images/Default.png",
            "/images/paddle_Default.png",
            "/images/red.png",
            "/images/redbreak1.png",
            "/images/redbreak2.png",
            "/images/background.png",
            "/images/while.png",
            "/images/bg.png",
            "/images/arkanoid_intro.png"
        );
    }

    /**
     * Gets a cached image without loading.
     * 
     * @param path The resource path
     * @return The cached Image, or null if not cached
     */
    public static Image getCachedImage(String path) {
        return imageCache.get(path);
    }

    /**
     * Checks if an image is already cached.
     * 
     * @param path The resource path
     * @return true if cached, false otherwise
     */
    public static boolean isCached(String path) {
        return imageCache.containsKey(path);
    }

    /**
     * Clears the image cache.
     */
    public static void clearCache() {
        imageCache.clear();
        logger.info("Image cache cleared");
    }

    /**
     * Gets the number of cached images.
     * 
     * @return The cache size
     */
    public static int getCacheSize() {
        return imageCache.size();
    }
}
