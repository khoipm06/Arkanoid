package com.arkanoid.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager.initialize();
    }

    @AfterEach
    void tearDown() throws SQLException {
        DatabaseManager.close();
    }

    @Test
    void testRegisterAndLogin() {
        String username = "testuser";
        String password = "password123";
        String email = "test@example.com";

        UserManager.User registeredUser = UserManager.register(username, password, email);
        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());

        UserManager.User loggedInUser = UserManager.login(username, password);
        assertNotNull(loggedInUser);
        assertEquals(username, loggedInUser.getUsername());
    }

    @Test
    void testUsernameExists() {
        String username = "existinguser";
        UserManager.register(username, "password", "email@email.com");
        assertTrue(UserManager.usernameExists(username));
        assertFalse(UserManager.usernameExists("nonexistinguser"));
    }
}
