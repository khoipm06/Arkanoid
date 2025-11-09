package com.arkanoid.database.repository;

import com.arkanoid.database.entity.GameSave;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GameSave data access
 */
public interface GameSaveRepository {
    /**
     * Create a new game save
     * 
     * @param userId             the user ID
     * @param saveName           the save name/description
     * @param levelNumber        the current level
     * @param score              the current score
     * @param lives              remaining lives
     * @param elapsedTimeSeconds game time elapsed
     * @param gameStateJson      serialized game state
     * @param thumbnailData      PNG thumbnail image data
     * @return the created game save
     */
    GameSave create(int userId, String saveName, int levelNumber, int score, int lives,
            int elapsedTimeSeconds, String gameStateJson, byte[] thumbnailData);

    /**
     * Find a game save by ID
     * 
     * @param id the save ID
     * @return Optional containing the save if found
     */
    Optional<GameSave> findById(int id);

    /**
     * Get all saves for a user, ordered by creation date descending
     * 
     * @param userId the user ID
     * @return list of game saves
     */
    List<GameSave> findByUserId(int userId);

    /**
     * Delete a game save
     * 
     * @param id the save ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(int id);

    /**
     * Get save count for a user
     * 
     * @param userId the user ID
     * @return number of saves
     */
    int countByUserId(int userId);
}
