package com.arkanoid.database;

import com.arkanoid.database.entity.UserPreferences;
import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.sql.*;
import java.util.Optional;

/**
 * Manager for user preferences operations.
 * Handles loading and saving music volume preferences.
 */
public class UserPreferencesManager {
    private static final Logger logger = GameLogger.getLogger(UserPreferencesManager.class);
    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();

    /**
     * Get user preferences. Creates default preferences if none exist.
     */
    public static Optional<UserPreferences> getPreferences(int userId) {
        String sql = "SELECT * FROM user_preferences WHERE user_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserPreferences prefs = new UserPreferences(userId);
                    prefs.setMusicVolume(rs.getInt("music_volume"));
                    return Optional.of(prefs);
                } else {
                    // Create default preferences
                    UserPreferences defaultPrefs = new UserPreferences(userId);
                    savePreferences(defaultPrefs);
                    return Optional.of(defaultPrefs);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get preferences for user {}", userId, e);
            return Optional.empty();
        } finally {
            // Connection is auto-closed by try-with-resources
        }
    }

    /**
     * Save user preferences (insert or update)
     */
    public static void savePreferences(UserPreferences prefs) {
        String sql = "INSERT INTO user_preferences (user_id, music_volume) VALUES (?, ?) " +
                     "ON CONFLICT(user_id) DO UPDATE SET music_volume = excluded.music_volume";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, prefs.getUserId());
            pstmt.setInt(2, prefs.getMusicVolume());

            pstmt.executeUpdate();
            logger.debug("Saved preferences for user {}: volume={}", prefs.getUserId(), String.format("%.2f", (double) prefs.getMusicVolume()));

        } catch (SQLException e) {
            logger.error("Failed to save preferences for user {}", prefs.getUserId(), e);
        }
    }

    /**
     * Update only music volume
     */
    public static void updateMusicVolume(int userId, int volume) {
        String sql = "UPDATE user_preferences SET music_volume = ? WHERE user_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Math.max(0, Math.min(100, volume)));
            pstmt.setInt(2, userId);

            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                // No row exists, insert default
                UserPreferences prefs = new UserPreferences(userId);
                prefs.setMusicVolume(volume);
                savePreferences(prefs);
            } else {
                logger.debug("Updated music volume for user {} to {}", userId, String.format("%.2f", (double) volume));
            }

        } catch (SQLException e) {
            logger.error("Failed to update music volume for user {}", userId, e);
        }
    }
}
