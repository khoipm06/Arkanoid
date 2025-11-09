package com.arkanoid.ui.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SessionManager and User authentication with ID tracking.
 */
class SessionManagerTest {

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
        assertEquals(1000, user.getMoney()); // Default starting money
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

        SessionManager.login(user);

        assertTrue(SessionManager.isLoggedIn());
        assertNotNull(SessionManager.getCurrentUser());
        assertEquals(5, SessionManager.getCurrentUser().getId());
        assertEquals("activePlayer", SessionManager.getCurrentUser().getUsername());
    }

    @Test
    void testLogout() {
        SessionManager.User user = new SessionManager.User(10, "temporaryUser");
        SessionManager.login(user);

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
        SessionManager.User user = new SessionManager.User(1, "richPlayer");

        assertEquals(1000, user.getMoney());

        user.addMoney(500);
        assertEquals(1500, user.getMoney());

        boolean spent = user.spendMoney(200);
        assertTrue(spent);
        assertEquals(1300, user.getMoney());
    }

    @Test
    void testUserSpendMoneyInsufficient() {
        SessionManager.User user = new SessionManager.User(1, "poorPlayer");

        assertEquals(1000, user.getMoney());

        boolean spent = user.spendMoney(1500); // Try to spend more than available
        assertFalse(spent);
        assertEquals(1000, user.getMoney()); // Money should not change
    }

    @Test
    void testUserSkinManagement() {
        SessionManager.User user = new SessionManager.User(1, "fashionista");

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
        SessionManager.User user = new SessionManager.User(1, "stylist");

        assertEquals("Default", user.getEquippedSkin());

        user.setEquippedSkin("RedBall");
        assertEquals("RedBall", user.getEquippedSkin());
    }

    @Test
    void testUserPaddleSkinManagement() {
        SessionManager.User user = new SessionManager.User(1, "paddleFan");

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
        SessionManager.User user = new SessionManager.User(1, "paddleStylist");

        assertEquals("paddle_Default", user.getEquippedPaddleSkin());

        user.setEquippedPaddleSkin("paddle_Red");
        assertEquals("paddle_Red", user.getEquippedPaddleSkin());
    }

    @Test
    void testSessionPersistenceAcrossOperations() {
        SessionManager.User user = new SessionManager.User(7, "persistentUser");
        SessionManager.login(user);

        // Perform various operations
        SessionManager.getCurrentUser().addMoney(500);
        SessionManager.getCurrentUser().addOwnedSkin("SpecialBall");
        SessionManager.setEquippedSkin("SpecialBall");

        // Verify all changes persist
        assertEquals(7, SessionManager.getCurrentUser().getId());
        assertEquals(1500, SessionManager.getCurrentUser().getMoney());
        assertTrue(SessionManager.getCurrentUser().hasSkin("SpecialBall"));
        assertEquals("SpecialBall", SessionManager.getCurrentUser().getEquippedSkin());
    }

    @Test
    void testMultipleLoginsClearPreviousSession() {
        SessionManager.User user1 = new SessionManager.User(1, "firstUser");
        SessionManager.User user2 = new SessionManager.User(2, "secondUser");

        SessionManager.login(user1);
        assertEquals(1, SessionManager.getCurrentUser().getId());

        SessionManager.login(user2);
        assertEquals(2, SessionManager.getCurrentUser().getId());
        assertEquals("secondUser", SessionManager.getCurrentUser().getUsername());
    }

    @Test
    void testUserIdIntegration() {
        // Simulate real usage: user logs in with database ID
        int databaseUserId = 123;
        SessionManager.User user = new SessionManager.User(databaseUserId, "realPlayer");
        SessionManager.login(user);

        // Game save system should be able to retrieve user ID
        int retrievedId = SessionManager.getCurrentUser().getId();
        assertEquals(databaseUserId, retrievedId);
    }

    @Test
    void testGuestUserScenario() {
        // Guest user with ID 0
        SessionManager.User guest = new SessionManager.User(0, "guest");
        SessionManager.login(guest);

        assertTrue(SessionManager.isLoggedIn());
        assertEquals(0, guest.getId());
        assertEquals("guest", guest.getUsername());
    }
}
