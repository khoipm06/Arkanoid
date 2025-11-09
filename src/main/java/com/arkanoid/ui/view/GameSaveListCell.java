package com.arkanoid.ui.view;

import com.arkanoid.database.entity.GameSave;
import com.arkanoid.systems.save.impl.ThumbnailCaptureImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom ListCell for displaying GameSave items with thumbnails.
 * Includes LRU caching for thumbnail images.
 */
public class GameSaveListCell extends ListCell<GameSave> {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final int THUMBNAIL_WIDTH = 100;
    private static final int THUMBNAIL_HEIGHT = 75;
    private static final int CACHE_MAX_SIZE = 20;

    // LRU Cache for thumbnails (LinkedHashMap with access order)
    private static final Map<Long, Image> thumbnailCache = new LinkedHashMap<Long, Image>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Image> eldest) {
            return size() > CACHE_MAX_SIZE;
        }
    };

    private final HBox content;
    private final ImageView thumbnailView;
    private final Label nameLabel;
    private final Label metadataLabel;
    private final Label timestampLabel;
    private final ThumbnailCaptureImpl thumbnailCapture;
    private Image defaultThumbnail;

    public GameSaveListCell() {
        this.thumbnailCapture = new ThumbnailCaptureImpl();

        // Create thumbnail ImageView
        thumbnailView = new ImageView();
        thumbnailView.setFitWidth(THUMBNAIL_WIDTH);
        thumbnailView.setFitHeight(THUMBNAIL_HEIGHT);
        thumbnailView.setPreserveRatio(false);
        thumbnailView.setSmooth(true);
        thumbnailView.getStyleClass().add("save-thumbnail");

        // Load default thumbnail
        try {
            defaultThumbnail = new Image(getClass().getResourceAsStream("/images/default_thumbnail.png"));
        } catch (Exception e) {
            System.err.println("Could not load default thumbnail: " + e.getMessage());
            defaultThumbnail = null;
        }

        // Create text labels
        nameLabel = new Label();
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.getStyleClass().add("save-name");

        metadataLabel = new Label();
        metadataLabel.setFont(Font.font("System", 12));
        metadataLabel.getStyleClass().add("save-metadata");

        timestampLabel = new Label();
        timestampLabel.setFont(Font.font("System", 11));
        timestampLabel.setStyle("-fx-text-fill: #888888;");
        timestampLabel.getStyleClass().add("save-timestamp");

        // Create text VBox
        VBox textBox = new VBox(4, nameLabel, metadataLabel, timestampLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(nameLabel, new Insets(0, 0, 2, 0));

        // Create HBox container
        content = new HBox(12, thumbnailView, textBox);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(8));
        content.getStyleClass().add("game-save-cell");
    }

    @Override
    protected void updateItem(GameSave save, boolean empty) {
        super.updateItem(save, empty);

        if (empty || save == null) {
            setGraphic(null);
            setText(null);
        } else {
            // Update text content
            nameLabel.setText(save.getSaveName());
            metadataLabel.setText(String.format("Level %d • Score: %,d • Lives: %d",
                    save.getLevelNumber(), save.getScore(), save.getLives()));
            timestampLabel.setText(save.getCreatedAt().format(TIMESTAMP_FORMATTER));

            // Load thumbnail (with caching)
            loadThumbnail(save);

            setGraphic(content);
            setText(null);
        }
    }

    private void loadThumbnail(GameSave save) {
        long saveId = save.getId();

        // Check cache first
        if (thumbnailCache.containsKey(saveId)) {
            thumbnailView.setImage(thumbnailCache.get(saveId));
            return;
        }

        // Load from byte array
        byte[] thumbnailData = save.getThumbnailData();
        Image thumbnail;

        if (thumbnailData != null && thumbnailData.length > 0) {
            try {
                thumbnail = thumbnailCapture.loadThumbnailFromBytes(thumbnailData);
                if (thumbnail != null) {
                    thumbnailCache.put(saveId, thumbnail);
                    thumbnailView.setImage(thumbnail);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Failed to load thumbnail for save " + saveId + ": " + e.getMessage());
            }
        }

        // Use default thumbnail
        thumbnailView.setImage(defaultThumbnail);
    }

    /**
     * Clear the thumbnail cache (useful when deleting saves or refreshing)
     */
    public static void clearCache() {
        thumbnailCache.clear();
    }

    /**
     * Remove a specific thumbnail from cache
     */
    public static void removeCachedThumbnail(Long saveId) {
        thumbnailCache.remove(saveId);
    }
}
