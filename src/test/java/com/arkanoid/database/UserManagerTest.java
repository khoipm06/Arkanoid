package com.arkanoid.database;

import com.arkanoid.database.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    @BeforeEach
    void setUp() throws SQLException {
        databaseManager.initialize();
        // Clear tables before each test
        try (Statement stmt = databaseManager.getConnection().createStatement()) {
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM player_profiles");
        }
    }

    @Test
    void testRegisterAndLogin() {
        String username = "testuser";
        String password = "password123";

        // Test successful registration
        User registeredUser = UserManager.register(username, password);
        assertNotNull(registeredUser, "Registration should be successful");
        assertEquals(username, registeredUser.getUsername());

        // Test successful login
        User loggedInUser = UserManager.login(username, password);
        assertNotNull(loggedInUser, "Login with correct credentials should succeed");
        assertEquals(username, loggedInUser.getUsername());

        // Test login with the wrong password
        User failedLoginUser = UserManager.login(username, "wrongpassword");
        assertNull(failedLoginUser, "Login with incorrect password should fail");
    }

    @Test
    void testRegisterDuplicateUser() {
        String username = "duplicateuser";
        String password = "password123";

        // First registration should succeed
        User registeredUser = UserManager.register(username, password);
        assertNotNull(registeredUser, "First registration should be successful");

        // Second registration with the same username should fail
        User duplicateUser = UserManager.register(username, "anotherpassword");
        assertNull(duplicateUser, "Registering a duplicate username should fail");
    }

    @Test
    void testUsernameExists() {
        String username = "existinguser";
        UserManager.register(username, "password123");
        assertTrue(UserManager.usernameExists(username), "usernameExists should return true for an existing user");
        assertFalse(UserManager.usernameExists("nonexistinguser"),
                "usernameExists should return false for a non-existing user");
    }
}
