package com.arkanoid.database;

import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserSessionStorage {
    private static final Logger logger = GameLogger.getLogger(UserSessionStorage.class);
    private static final Path SESSION_FILE = Paths.get("data", "user_session.dat");
    
    public static boolean saveSession(int userId, String username) {
        try {
            Files.createDirectories(SESSION_FILE.getParent());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(SESSION_FILE.toFile()))) {
                writer.write(String.valueOf(userId));
                writer.newLine();
                writer.write(username);
            }
            logger.info("Saved session: {} (ID: {})", username, userId);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save session: {}", e.getMessage());
            return false;
        }
    }
    
    public static SessionData loadSession() {
        if (!Files.exists(SESSION_FILE)) {
            logger.debug("No saved session found");
            return null;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(SESSION_FILE.toFile()))) {
            String userIdStr = reader.readLine();
            String username = reader.readLine();
            if (userIdStr != null && username != null) {
                int userId = Integer.parseInt(userIdStr);
                logger.info("Loaded session: {} (ID: {})", username, userId);
                return new SessionData(userId, username);
            }
        } catch (IOException | NumberFormatException e) {
            logger.error("Failed to load session: {}", e.getMessage());
        }
        return null;
    }
    
    public static boolean clearSession() {
        try {
            if (Files.exists(SESSION_FILE)) {
                Files.delete(SESSION_FILE);
                logger.info("Cleared session");
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to clear session: {}", e.getMessage());
            return false;
        }
    }

    public static boolean hasSession() {
        return Files.exists(SESSION_FILE);
    }

    public static class SessionData {
        private final int userId;
        private final String username;

        public SessionData(int userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public int getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }
    }
}
