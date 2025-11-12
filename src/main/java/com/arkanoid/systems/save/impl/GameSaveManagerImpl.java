package com.arkanoid.systems.save.impl;

import com.arkanoid.database.RepositoryFactory;
import com.arkanoid.database.entity.GameSave;
import com.arkanoid.database.repository.GameSaveRepository;
import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.save.*;
import com.arkanoid.utils.CompressionUtil;
import org.slf4j.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.WritableImage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of GameSaveManager interface.
 * Handles save/load operations with database persistence.
 */
public class GameSaveManagerImpl implements GameSaveManager {
    private static final Logger logger = GameLogger.getLogger(GameSaveManagerImpl.class);
    private final GameStateSerializer serializer;
    private final ThumbnailCapture thumbnailCapture;
    private final GameManager gameManager;
    private final GameSaveRepository repository;

    /**
     * Creates a new GameSaveManager instance.
     * 
     * @param gameManager The game manager to extract/restore game state from
     */
    public GameSaveManagerImpl(GameManager gameManager) {
        this.gameManager = gameManager;
        this.serializer = new GameStateSerializerImpl();
        this.thumbnailCapture = new ThumbnailCaptureImpl();
        this.repository = RepositoryFactory.getInstance().getGameSaveRepository();
    }

    /**
     * Saves the current game state to the database.
     * Game must be paused to save.
     * 
     * @param userId   The user ID who owns this save
     * @param saveName The name for this save (1-50 characters)
     * @param canvas   The game canvas for thumbnail capture (can be null)
     * @return The created GameSave entity
     * @throws IllegalStateException if game is not in a saveable state
     * @throws RuntimeException      if save operation fails
     */
    @Override
    public GameSave saveCurrentGame(int userId, String saveName, WritableImage canvas) {
        if (!canSaveCurrentGame()) {
            throw new IllegalStateException("Cannot save game in current state");
        }

        try {
            // Extract current game state
            GameState gameState = gameManager.extractCurrentGameState();

            // Validate game state
            if (!serializer.isValidGameState(gameState)) {
                throw new IllegalStateException("Invalid game state - cannot save");
            }

            // Serialize to JSON
            String stateJson = serializer.toJson(gameState);
            int originalSize = stateJson.length();
            
            // Compress game state using LZ4
            long compressStart = System.currentTimeMillis();
            byte[] compressedGameState = CompressionUtil.compress(stateJson);
            long compressDuration = System.currentTimeMillis() - compressStart;
            
            logger.debug("Game state compressed: {} -> {} bytes ({} compression ratio) - {}ms",
                    originalSize, compressedGameState.length, 
                    String.format("%.1f%%", (1 - (double)compressedGameState.length / originalSize) * 100),
                    compressDuration);

            // Capture thumbnail (if canvas provided)
            byte[] thumbnailPng = null;
            if (canvas != null) {
                try {
                    long thumbStart = System.currentTimeMillis();
                    thumbnailPng = thumbnailCapture.captureThumbnailPNG(canvas);
                    long thumbDuration = System.currentTimeMillis() - thumbStart;
                    logger.debug("Thumbnail captured: {} bytes - {}ms",
                            thumbnailPng.length, thumbDuration);
                } catch (IOException e) {
                    logger.error("Failed to capture thumbnail: {}", e.getMessage());
                    // Continue without thumbnail
                }
            }

            // Check save count and delete oldest if needed
            int saveCount = getSaveCount(userId);
            if (saveCount >= MAX_SAVES_PER_USER) {
                deleteOldestSave(userId);
            }

            // Save to database using repository API with compressed data
            long startTime = System.currentTimeMillis();
            GameSave savedGame = repository.create(
                    userId,
                    saveName,
                    gameState.getLevelNumber(),
                    gameState.getScore(),
                    gameState.getLives(),
                    gameState.getElapsedTimeSeconds(),
                    compressedGameState,
                    thumbnailPng);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Game saved successfully: '{}' (Level {}, Score {}) - Compressed {}KB -> {}KB - {}ms",
                    saveName, gameState.getLevelNumber(), gameState.getScore(), 
                    originalSize / 1024, compressedGameState.length / 1024, duration);
            return savedGame;

        } catch (Exception e) {
            logger.error("Failed to save game: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save game: " + e.getMessage(), e);
        }
    }

    /**
     * Saves the current game with an auto-generated name.
     * Format: "Level {levelNum} - {MM-dd HH:mm}"
     * 
     * @param userId The user ID who owns this save
     * @param canvas The game canvas for thumbnail capture (can be null)
     * @return The created GameSave entity
     */
    @Override
    public GameSave saveCurrentGameWithAutoName(int userId, WritableImage canvas) {
        GameState gameState = gameManager.extractCurrentGameState();
        String autoName = String.format(DEFAULT_SAVE_NAME_FORMAT,
                gameState.getLevelNumber(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        return saveCurrentGame(userId, autoName, canvas);
    }

    /**
     * Loads a saved game by ID and restores the game state.
     * Validates JSON integrity and game state before restoring.
     * 
     * @param saveId The ID of the save to load
     * @return The loaded GameSave entity
     * @throws IllegalArgumentException if save not found
     * @throws IllegalStateException    if save data is corrupted
     * @throws RuntimeException         if load operation fails
     */
    @Override
    public GameSave loadGame(int saveId) {
        try {
            // Fetch save from database
            Optional<GameSave> saveOpt = repository.findById(saveId);
            if (saveOpt.isEmpty()) {
                throw new IllegalArgumentException("Save not found: " + saveId);
            }

            GameSave gameSave = saveOpt.get();
            byte[] compressedGameState = gameSave.getCompressedGameState();
            
            // Decompress game state using LZ4
            long decompressStart = System.currentTimeMillis();
            String stateJson = com.arkanoid.utils.CompressionUtil.decompress(compressedGameState);
            long decompressDuration = System.currentTimeMillis() - decompressStart;
            
            logger.debug("Game state decompressed: {} -> {} bytes - {}ms",
                    compressedGameState.length, stateJson.length(), decompressDuration);

            // Validate JSON
            if (!serializer.isValidJson(stateJson)) {
                throw new IllegalStateException("Save file is corrupted - invalid JSON");
            }

            // Deserialize game state
            GameState gameState = serializer.fromJson(stateJson);

            // Validate deserialized state
            if (!serializer.isValidGameState(gameState)) {
                throw new IllegalStateException("Save file is corrupted - invalid game state");
            }

            // Restore game state
            long startTime = System.currentTimeMillis();
            gameManager.restoreGameState(gameState);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Game loaded successfully: '{}' (Level {}, Score {}, {} balls, {} bricks) - {}KB compressed - {}ms",
                    gameSave.getSaveName(), gameState.getLevelNumber(), gameState.getScore(),
                    gameState.getBallStates().size(), gameState.getBrickStates().size(),
                    compressedGameState.length / 1024, duration);
            return gameSave;

        } catch (Exception e) {
            logger.error("Failed to load game: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load game: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all saves for a specific user.
     * 
     * @param userId The user ID to get saves for
     * @return ObservableList of GameSave entities (empty if none found)
     */
    @Override
    public ObservableList<GameSave> getAllSaves(int userId) {
        try {
            List<GameSave> saves = repository.findByUserId(userId);
            return FXCollections.observableArrayList(saves);
        } catch (Exception e) {
            logger.error("Failed to get saves: {}", e.getMessage());
            return FXCollections.observableArrayList();
        }
    }

    @Override
    public Optional<GameSave> getSaveById(int saveId) {
        return repository.findById(saveId);
    }

    @Override
    public int getSaveCount(int userId) {
        return repository.countByUserId(userId);
    }

    /**
     * Deletes a save by ID.
     * 
     * @param saveId The ID of the save to delete
     * @throws RuntimeException if delete operation fails
     */
    @Override
    public void deleteSave(int saveId) {
        try {
            Optional<GameSave> save = repository.findById(saveId);
            boolean deleted = repository.deleteById(saveId);
            if (deleted) {
                String saveName = save.map(GameSave::getSaveName).orElse("Unknown");
                logger.info("Save deleted: '{}' (ID: {})", saveName, saveId);
            } else {
                logger.error("Save not found: {}", saveId);
            }
        } catch (Exception e) {
            logger.error("Failed to delete save: {}", e.getMessage());
            throw new RuntimeException("Failed to delete save: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAllSaves(int userId) {
        try {
            List<GameSave> saves = repository.findByUserId(userId);
            for (GameSave save : saves) {
                repository.deleteById(save.getId());
            }
            logger.info("All saves deleted for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to delete all saves: {}", e.getMessage());
            throw new RuntimeException("Failed to delete all saves: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the current game state is saveable.
     * Game must be paused to save.
     * 
     * @return true if game can be saved, false otherwise
     */
    @Override
    public boolean canSaveCurrentGame() {
        // Check if game is in a valid state to save
        if (gameManager == null) {
            return false;
        }

        // Can save if game is paused
        GameManager.GameState currentState = gameManager.getCurrentState();
        return currentState == GameManager.GameState.PAUSED;
    }

    /**
     * Validates a save name.
     * Must be 1-50 characters after trimming.
     * 
     * @param saveName The name to validate
     * @return true if valid, false otherwise
     */
    @Override
    public boolean isValidSaveName(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            return false;
        }

        String trimmed = saveName.trim();
        return trimmed.length() >= 1 && trimmed.length() <= 50;
    }

    /**
     * Deletes the oldest save for a user.
     */
    private void deleteOldestSave(int userId) {
        try {
            List<GameSave> saves = repository.findByUserId(userId);
            if (!saves.isEmpty()) {
                // Saves are ordered by timestamp DESC, so last is oldest
                GameSave oldest = saves.get(saves.size() - 1);
                repository.deleteById(oldest.getId());
                logger.info("Auto-deleted oldest save: {}", oldest.getSaveName());
            }
        } catch (Exception e) {
            logger.error("Failed to delete oldest save: {}", e.getMessage());
        }
    }
}
