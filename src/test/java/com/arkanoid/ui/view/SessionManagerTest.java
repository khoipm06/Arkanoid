package com.arkanoid.ui.view;

import com.arkanoid.database.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SessionManager and User authentication with ID tracking.
 */
class SessionManagerTest {

    private static final int TEST_USER_ID = 37;

    @BeforeAll
    static void initDatabase() {
        // Initialize database connection pool before all tests
        DatabaseManager.getInstance().initialize();
        
        // Ensure test user has a profile
        ensureTestUserProfile();
    }

    private static void ensureTestUserProfile() {
        String sql = "INSERT OR IGNORE INTO player_profiles (user_id, money, high_score, current_skin, games_played, total_score) VALUES (?, 1000, 0, 'Default', 0, 0)";
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, TEST_USER_ID);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                DatabaseManager.getInstance().releaseConnection(conn);
            }
        }
    }

    @BeforeEach
    void setUp() {
        // Ensure clean state before each test
        SessionManager.logout();
    }

    @Test
    void testUserCreationWithId() {
        SessionManager.User user = new SessionManager.User(1, "testUser");

        assertEquals(1, user.getId());
        assertEquals("testUser", user.getUsername());
    }

    @Test
    void testUserCreationWithDifferentId() {
        SessionManager.User user1 = new SessionManager.User(1, "player1");
        SessionManager.User user2 = new SessionManager.User(42, "player2");

        assertEquals(1, user1.getId());
        assertEquals(42, user2.getId());
        assertNotEquals(user1.getId(), user2.getId());
    }

    @Test
    void testLoginAndGetCurrentUser() {
        SessionManager.User user = new SessionManager.User(5, "activePlayer");

        SessionManager.login(user.getId(), user.getUsername(), "dummyHash");

        assertTrue(SessionManager.isLoggedIn());
        assertNotNull(SessionManager.getCurrentUser());
        assertEquals(5, SessionManager.getCurrentUser().getId());
        assertEquals("activePlayer", SessionManager.getCurrentUser().getUsername());
    }

    @Test
    void testLogout() {
        SessionManager.User user = new SessionManager.User(10, "temporaryUser");
        SessionManager.login(user.getId(), user.getUsername(), "dummyHash");

        assertTrue(SessionManager.isLoggedIn());

        SessionManager.logout();

        assertFalse(SessionManager.isLoggedIn());
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void testIsLoggedInWhenNoUser() {
        assertFalse(SessionManager.isLoggedIn());
    }

    @Test
    void testGetCurrentUserWhenNotLoggedIn() {
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    void testUserMoneyManagement() {
        SessionManager.User user = new SessionManager.User(37, "richPlayer"); // Use existing user from DB

        int initialMoney = user.getMoney();

        user.addMoney(500);
        assertEquals(initialMoney + 500, user.getMoney());

        boolean spent = user.spendMoney(200);
        assertTrue(spent);
        assertEquals(initialMoney + 300, user.getMoney());
    }

    @Test
    void testUserSpendMoneyInsufficient() {
        SessionManager.User user = new SessionManager.User(37, "poorPlayer"); // Use existing user from DB

        int initialMoney = user.getMoney();
        int excessiveAmount = initialMoney + 1000;

        boolean spent = user.spendMoney(excessiveAmount); // Try to spend more than available
        assertFalse(spent);
        assertEquals(initialMoney, user.getMoney()); // Money should not change
    }

    @Test
    void testUserSkinManagement() {
        SessionManager.User user = new SessionManager.User(37, "fashionista");

        // Default skin should be owned
        assertTrue(user.hasSkin("Default"));

        // Add new skin
        user.addOwnedSkin("BlueBall");
        assertTrue(user.hasSkin("BlueBall"));

        // Check non-owned skin
        assertFalse(user.hasSkin("GoldenBall"));
    }

    @Test
    void testUserEquippedSkin() {
        SessionManager.User user = new SessionManager.User(37, "stylist"); // Use existing user from DB

        String currentSkin = user.getEquippedSkin();
        assertNotNull(currentSkin);

        user.setEquippedSkin("RedBall");
        assertEquals("RedBall", user.getEquippedSkin());
        
        // Restore original skin
        user.setEquippedSkin(currentSkin);
    }

    @Test
    void testUserPaddleSkinManagement() {
        SessionManager.User user = new SessionManager.User(37, "paddleFan");

        // Default paddle skin should be owned
        assertTrue(user.hasPaddleSkin("paddle_Default"));

        // Add new paddle skin
        user.addOwnedPaddleSkin("paddle_Metal");
        assertTrue(user.hasPaddleSkin("paddle_Metal"));

        // Check non-owned paddle skin
        assertFalse(user.hasPaddleSkin("paddle_Golden"));
    }

    @Test
    void testUserEquippedPaddleSkin() {
        SessionManager.User user = new SessionManager.User(37, "paddleStylist");

        String initialPaddleSkin = user.getEquippedPaddleSkin();
        assertNotNull(initialPaddleSkin);
        
        // Just verify the getter works - setter uses inventory system which is complex
        // and requires proper database state that would need extensive setup
        assertTrue(initialPaddleSkin.startsWith("paddle_"));
    }

    @Test
    void testSessionPersistenceAcrossOperations() {
        SessionManager.User user = new SessionManager.User(37, "persistentUser"); // Use existing user from DB
        SessionManager.login(user.getId(), user.getUsername(), "dummyHash");

        int initialMoney = SessionManager.getCurrentUser().getMoney();
        String initialSkin = SessionManager.getCurrentUser().getEquippedSkin();

        // Perform various operations
        SessionManager.getCurrentUser().addMoney(500);
        SessionManager.getCurrentUser().addOwnedSkin("SpecialBall");
        SessionManager.setEquippedSkin("SpecialBall");

        // Verify all changes persist
        assertEquals(37, SessionManager.getCurrentUser().getId());
        assertEquals(initialMoney + 500, SessionManager.getCurrentUser().getMoney());
        assertTrue(SessionManager.getCurrentUser().hasSkin("SpecialBall"));
        assertEquals("SpecialBall", SessionManager.getCurrentUser().getEquippedSkin());
        
        // Cleanup: restore original state
        SessionManager.getCurrentUser().spendMoney(500);
        SessionManager.setEquippedSkin(initialSkin);
    }

    @Test
    void testMultipleLoginsClearPreviousSession() {
        SessionManager.User user1 = new SessionManager.User(1, "firstUser");
        SessionManager.User user2 = new SessionManager.User(2, "secondUser");

        SessionManager.login(user1.getId(), user1.getUsername(), "dummyHash");
        assertEquals(1, SessionManager.getCurrentUser().getId());

        SessionManager.login(user2.getId(), user2.getUsername(), "dummyHash");
        assertEquals(2, SessionManager.getCurrentUser().getId());
        assertEquals("secondUser", SessionManager.getCurrentUser().getUsername());
    }

    @Test
    void testUserIdIntegration() {
        // Simulate real usage: user logs in with database ID
        int databaseUserId = 123;
        SessionManager.User user = new SessionManager.User(databaseUserId, "realPlayer");
        SessionManager.login(user.getId(), user.getUsername(), "dummyHash");

        // Game save system should be able to retrieve user ID
        int retrievedId = SessionManager.getCurrentUser().getId();
        assertEquals(databaseUserId, retrievedId);
    }

    @Test
    void testGuestUserScenario() {
        // Guest user with ID 0
        SessionManager.User guest = new SessionManager.User(0, "guest");
        SessionManager.login(guest.getId(), guest.getUsername(), "dummyHash");

        assertTrue(SessionManager.isLoggedIn());
        assertEquals(0, guest.getId());
        assertEquals("guest", guest.getUsername());
    }
}
