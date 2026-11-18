package com.arkanoid.utils;

import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.threading.ThreadManager;
import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe utility class for loading and caching images synchronously and asynchronously.
 */
public final class ImageLoader {
    private static final Logger logger = GameLogger.getLogger(ImageLoader.class);
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CompletableFuture<Image>> inFlightLoads = new ConcurrentHashMap<>();

    private ImageLoader() {
        // Utility class - prevent instantiation
    }

    /**
     * Loads an image synchronously from the classpath and caches it.
     * Always closes the underlying stream and returns {@code null} safely on missing
     * resources or headless environments without throwing uncaught exceptions.
     *
     * @param path The resource path (e.g., "/images/Default.png")
     * @return The loaded Image, or {@code null} if loading fails
     */
    public static Image loadImage(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        Image cached = imageCache.get(path);
        if (cached != null) {
            return cached;
        }
        return imageCache.computeIfAbsent(path, key -> {
            try (InputStream is = ImageLoader.class.getResourceAsStream(key)) {
                if (is == null) {
                    logger.warn("Image not found: {}", key);
                    return null;
                }
                Image img = new Image(is);
                logger.debug("Loaded image: {}", key);
                return img;
            } catch (Exception | LinkageError e) {
                logger.warn("Failed to load image '{}': {}", key, e.getMessage());
                return null;
            }
        });
    }

    /**
     * Loads an image asynchronously, reading bytes on a background thread and decoding on the FX thread.
     * Concurrent requests for the same path share the in-flight future.
     *
     * @param path The resource path (e.g., "/images/Default.png")
     * @return CompletableFuture that completes with the loaded Image
     */
    public static CompletableFuture<Image> loadImageAsync(String path) {
        if (path == null || path.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        Image cached = imageCache.get(path);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return inFlightLoads.computeIfAbsent(path, key -> {
            CompletableFuture<Image> future = new CompletableFuture<>();
            future.whenComplete((img, ex) -> inFlightLoads.remove(key, future));

            try {
                ThreadManager.getInstance().executeBackground(() -> {
                    try (InputStream is = ImageLoader.class.getResourceAsStream(key)) {
                        if (is == null) {
                            logger.warn("Image not found: {}", key);
                            future.completeExceptionally(new IllegalArgumentException("Image not found: " + key));
                            return;
                        }
                        byte[] bytes = is.readAllBytes();

                        Platform.runLater(() -> {
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                                Image img = new Image(bais);
                                imageCache.putIfAbsent(key, img);
                                logger.debug("Loaded image async: {}", key);
                                future.complete(img);
                            } catch (Exception | LinkageError e) {
                                logger.error("Failed to decode image '{}': {}", key, e.getMessage(), e);
                                future.completeExceptionally(e);
                            }
                        });
                    } catch (Exception e) {
                        logger.error("Failed to load image '{}': {}", key, e.getMessage(), e);
                        future.completeExceptionally(e);
                    }
                }, "LoadImage");
            } catch (Exception e) {
                future.completeExceptionally(e);
            }

            return future;
        });
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
        return path != null ? imageCache.get(path) : null;
    }

    /**
     * Checks if an image is already cached.
     *
     * @param path The resource path
     * @return true if cached, false otherwise
     */
    public static boolean isCached(String path) {
        return path != null && imageCache.containsKey(path);
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
