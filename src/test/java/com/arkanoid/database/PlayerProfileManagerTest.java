package com.arkanoid.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class PlayerProfileManagerTest {

    private int userId;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager.initialize();
        UserManager.User user = UserManager.register("profileuser", "password", "profile@example.com");
        assertNotNull(user);
        userId = user.getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        DatabaseManager.close();
    }

    @Test
    void testGetProfile() {
        PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(userId);
        assertNotNull(profile);
        assertEquals(0, profile.coins);
        assertEquals(0, profile.highScore);
    }

    @Test
    void testUpdateCoins() {
        PlayerProfileManager.updateCoins(userId, 100);
        PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(userId);
        assertEquals(100, profile.coins);
    }

    @Test
    void testUpdateHighScore() {
        PlayerProfileManager.updateHighScore(userId, 500);
        PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(userId);
        assertEquals(500, profile.highScore);

        // Test that it doesn't update with a lower score
        PlayerProfileManager.updateHighScore(userId, 400);
        profile = PlayerProfileManager.getProfile(userId);
        assertEquals(500, profile.highScore);
    }
}
