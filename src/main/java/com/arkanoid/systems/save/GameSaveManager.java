package com.arkanoid.systems.save;

import com.arkanoid.database.entity.GameSave;
import javafx.collections.ObservableList;
import javafx.scene.image.WritableImage;

import java.util.Optional;

/**
 * Contract for managing game save/load operations.
 * 
 * <p>
 * This interface defines the high-level save system API that coordinates
 * between the game state, database persistence, and thumbnail capture.
 * 
 * <p>
 * <b>Implementation Requirements:</b>
 * <ul>
 * <li>Must enforce MAX_SAVES_PER_USER limit (100) by auto-deleting oldest
 * saves</li>
 * <li>All database operations must use transactions (via DatabaseManager)</li>
 * <li>Save operations must be asynchronous (JavaFX Task) to avoid UI
 * blocking</li>
 * <li>Load operations should be fast enough to run on UI thread (&lt;100ms
 * typical)</li>
 * <li>Must handle errors gracefully with user-friendly messages</li>
 * </ul>
 * 
 * <p>
 * <b>Thread Safety:</b> Implementations must be thread-safe for concurrent
 * access
 * from JavaFX application thread and background task threads.
 * 
 * <p>
 * <b>Error Handling:</b> All methods may throw:
 * <ul>
 * <li>{@code IllegalStateException} if game state is invalid</li>
 * <li>{@code DatabaseException} if persistence fails</li>
 * <li>{@code JsonSyntaxException} if serialization fails</li>
 * </ul>
 * 
 * @author Game Save System Specification
 * @version 1.0
 * @see com.arkanoid.database.entity.GameSave
 * @see com.arkanoid.systems.save.GameStateSerializer
 */
public interface GameSaveManager {

    /**
     * Maximum number of saves allowed per user.
     * When exceeded, the oldest save is automatically deleted.
     */
    int MAX_SAVES_PER_USER = 100;

    /**
     * Default save name format when user doesn't provide a custom name.
     */
    String DEFAULT_SAVE_NAME_FORMAT = "Level %d - %s";

    // ==================== Save Operations ====================

    /**
     * Saves the current game state with a user-provided name.
     * 
     * <p>
     * This method captures:
     * <ul>
     * <li>Current game state (level, score, lives, positions, velocities)</li>
     * <li>Thumbnail PNG from canvas snapshot</li>
     * <li>Timestamp (current system time)</li>
     * </ul>
     * 
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Validate save name (1-50 characters)</li>
     * <li>Capture thumbnail from canvas</li>
     * <li>Serialize game state to JSON</li>
     * <li>Save to database in transaction:
     * <ul>
     * <li>Insert new GameSave record</li>
     * <li>If count > MAX_SAVES_PER_USER: delete oldest</li>
     * <li>Commit transaction</li>
     * </ul>
     * </li>
     * <li>Refresh save list observable</li>
     * </ol>
     * 
     * <p>
     * <b>Performance:</b> Target &lt;2s for typical game state (50 bricks).
     * Uses background thread to avoid blocking UI.
     * 
     * @param userId   the ID of the user saving the game
     * @param saveName custom name for the save (1-50 characters)
     * @param canvas   the current game canvas for thumbnail capture
     * @return the created GameSave entity with assigned ID
     * @throws IllegalArgumentException if saveName is empty or > 50 chars
     * @throws IllegalStateException    if game state is not saveable (e.g., between
     *                                  levels)
     * @throws DatabaseException        if save operation fails
     */
    GameSave saveCurrentGame(int userId, String saveName, WritableImage canvas);

    /**
     * Saves the current game state with an auto-generated name.
     * 
     * <p>
     * Generated name format: "Level {levelNum} - {timestamp}"
     * Example: "Level 3 - 2025-11-14 14:32"
     * 
     * @param userId the ID of the user saving the game
     * @param canvas the current game canvas for thumbnail capture
     * @return the created GameSave entity
     * @throws IllegalStateException if game state is not saveable
     * @throws DatabaseException     if save operation fails
     * @see #saveCurrentGame(int, String, WritableImage)
     */
    GameSave saveCurrentGameWithAutoName(int userId, WritableImage canvas);

    // ==================== Load Operations ====================

    /**
     * Loads a saved game by its ID and restores the game state.
     * 
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Fetch GameSave record from database</li>
     * <li>Deserialize JSON to GameState object</li>
     * <li>Restore GameManager state:
     * <ul>
     * <li>Set level number, score, lives, elapsed time</li>
     * <li>Recreate paddle from PaddleState</li>
     * <li>Recreate balls from List&lt;BallState&gt;</li>
     * <li>Recreate bricks from List&lt;BrickState&gt;</li>
     * <li>Recreate power-ups from List&lt;PowerUpState&gt;</li>
     * </ul>
     * </li>
     * <li>Validate restored state (positions within bounds, etc.)</li>
     * </ol>
     * 
     * <p>
     * <b>Performance:</b> Target &lt;1s for typical game state.
     * 
     * <p>
     * <b>Validation:</b> If loaded state is invalid (corrupted JSON, out-of-bounds
     * positions), throws IllegalStateException with diagnostic message.
     * 
     * @param saveId the unique ID of the save to load
     * @return the loaded GameSave entity
     * @throws EntityNotFoundException if saveId doesn't exist
     * @throws IllegalStateException   if loaded state is invalid/corrupted
     * @throws JsonSyntaxException     if JSON deserialization fails
     * @throws DatabaseException       if database read fails
     */
    GameSave loadGame(int saveId);

    // ==================== Query Operations ====================

    /**
     * Retrieves all saves for the current user, ordered by creation time (newest
     * first).
     * 
     * <p>
     * Returns an {@code ObservableList} that can be bound to a ListView for
     * automatic UI updates when saves are added/removed.
     * 
     * <p>
     * <b>Performance:</b> This method does NOT load thumbnail images or JSON.
     * Thumbnails are loaded on-demand when ListView cells become visible.
     * 
     * @param userId the ID of the user
     * @return observable list of GameSave entities (without thumbnails loaded)
     */
    ObservableList<GameSave> getAllSaves(int userId);

    /**
     * Retrieves a specific save by ID (for preview/inspection).
     * 
     * @param saveId the unique ID of the save
     * @return the GameSave entity, or empty if not found
     */
    Optional<GameSave> getSaveById(int saveId);

    /**
     * Gets the current save count for a user.
     * Used to display "23/100 saves" in UI.
     * 
     * @param userId the ID of the user
     * @return number of saves (0-100)
     */
    int getSaveCount(int userId);

    // ==================== Delete Operations ====================

    /**
     * Deletes a save by ID.
     * 
     * <p>
     * This operation is permanent and cannot be undone.
     * UI should prompt for confirmation before calling this method.
     * 
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Delete record from database</li>
     * <li>Remove from observable list (triggers ListView update)</li>
     * <li>Clear thumbnail from cache if present</li>
     * </ol>
     * 
     * @param saveId the unique ID of the save to delete
     * @throws EntityNotFoundException if saveId doesn't exist
     * @throws DatabaseException       if delete operation fails
     */
    void deleteSave(int saveId);

    /**
     * Deletes all saves for a user.
     * Used when user resets their profile or deletes their account.
     * 
     * @param userId the ID of the user
     * @throws DatabaseException if batch delete fails
     */
    void deleteAllSaves(int userId);

    // ==================== Validation ====================

    /**
     * Checks if the current game state can be saved.
     * 
     * <p>
     * Returns false if:
     * <ul>
     * <li>Game is not in progress (paused/between levels/game over)</li>
     * <li>No user is logged in</li>
     * <li>Game state is corrupted/invalid</li>
     * </ul>
     * 
     * @return true if game can be saved, false otherwise
     */
    boolean canSaveCurrentGame();

    /**
     * Validates a save name according to business rules.
     * 
     * <p>
     * Rules:
     * <ul>
     * <li>Length: 1-50 characters</li>
     * <li>Cannot be only whitespace</li>
     * <li>No special characters that break filesystem compatibility (handled by
     * DB)</li>
     * </ul>
     * 
     * @param saveName the name to validate
     * @return true if valid, false otherwise
     */
    boolean isValidSaveName(String saveName);
}
