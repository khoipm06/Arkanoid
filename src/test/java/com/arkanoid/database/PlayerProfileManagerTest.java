package com.arkanoid.database;

import com.arkanoid.database.entity.User;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerProfileManagerTest {

    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();

    @BeforeEach
    void setUp() throws SQLException {
        databaseManager.initialize();
        try (Statement stmt = databaseManager.getConnection().createStatement()) {
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM player_profiles");
        }
    }

    @Test
    void testUpdateHighScore() {
        User user = UserManager.register("scoreuser", "pw");
        assertNotNull(user);
        int userId = user.getId();

        PlayerProfileManager.updateHighScore(userId, 500);
        PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(userId);
        assertEquals(500, profile.highScore);

        // Test that it doesn't update with a lower score
        PlayerProfileManager.updateHighScore(userId, 400);
        profile = PlayerProfileManager.getProfile(userId);
        assertEquals(500, profile.highScore);
    }

    @Test
    void testGetLeaderboardData() {
        // Create some users with scores
        User user1 = UserManager.register("player1", "pw");
        User user2 = UserManager.register("player2", "pw");
        User user3 = UserManager.register("player3", "pw");

        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(user3);

        // Manually update high scores as the game logic is not part of this test
        updateHighScoreInDb(user1.getId(), 1500);
        updateHighScoreInDb(user2.getId(), 2500);
        updateHighScoreInDb(user3.getId(), 1000);

        List<PlayerProfileManager.LeaderboardEntry> leaderboard = PlayerProfileManager.getLeaderboardData();

        assertNotNull(leaderboard);
        assertEquals(3, leaderboard.size());

        // Check if the list is sorted by high score descending
        assertEquals("player2", leaderboard.get(0).username);
        assertEquals(2500, leaderboard.get(0).highScore);

        assertEquals("player1", leaderboard.get(1).username);
        assertEquals(1500, leaderboard.get(1).highScore);

        assertEquals("player3", leaderboard.get(2).username);
        assertEquals(1000, leaderboard.get(2).highScore);
    }

    // Helper to directly update high score for testing purposes
    private void updateHighScoreInDb(int userId, int score) {
        String sql = "UPDATE player_profiles SET high_score = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = databaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, score);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to update high score in DB", e);
        }
    }
}
