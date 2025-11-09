package com.arkanoid.database.repository;

import com.arkanoid.database.entity.PlayerProfile;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PlayerProfile data access
 */
public interface PlayerProfileRepository {
    /**
     * Create a default profile for a user
     * 
     * @param userId the user ID
     * @return the created profile
     */
    PlayerProfile create(int userId);

    /**
     * Find a player profile by user ID
     * 
     * @param userId the user ID
     * @return Optional containing the profile if found
     */
    Optional<PlayerProfile> findByUserId(int userId);

    /**
     * Update a player profile
     * 
     * @param profile the profile to update
     */
    void update(PlayerProfile profile);

    /**
     * Get leaderboard data (top players by high score)
     * 
     * @param limit maximum number of entries to return
     * @return list of profiles ordered by high score descending
     */
    List<PlayerProfile> getLeaderboard(int limit);
}
