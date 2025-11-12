package com.arkanoid.database;

import com.arkanoid.database.entity.User;
import com.arkanoid.database.repository.UserRepository;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.utils.CompressionUtil;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Secure session storage with LZ4 compression.
 * Uses CompressionUtil for compression/decompression operations.
 */
public class UserSessionStorage {
    private static final Logger logger = GameLogger.getLogger(UserSessionStorage.class);
    private static final Path SESSION_FILE = Paths.get("data", "user_session.bin");
    private static final UserRepository userRepository = RepositoryFactory.getInstance().getUserRepository();
    
    public static boolean saveSession(int userId, String username, String passwordHash) {
        try {
            Files.createDirectories(SESSION_FILE.getParent());
            
            // Aggregate data (userId|username|passwordHash)
            String sessionData = String.format("%d|%s|%s", userId, username, passwordHash);
            
            // Compress and write to file using LZ4 Frame format
            CompressionUtil.compressToFile(sessionData, SESSION_FILE.toFile());
            
            logger.info("Saved session: {} (ID: {}) - Compressed to LZ4 frame format", username, userId);
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
        
        try {
            // Decompress from LZ4 Frame format file
            String sessionData = CompressionUtil.decompressFromFile(SESSION_FILE.toFile());

            // Parse data, must use double escape for regex
            String[] parts = sessionData.split("\\|");
            
            if (parts.length != 3) {
                logger.warn("Invalid session format");
                return null;
            }
            
            int userId = Integer.parseInt(parts[0]);
            String username = parts[1];
            String storedPasswordHash = parts[2];
            
            // Verify password hash matches database (prevents tampering)
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.warn("User not found in database: {}", userId);
                clearSession();
                return null;
            }
            
            User user = userOpt.get();
            if (!user.getPasswordHash().equals(storedPasswordHash)) {
                logger.warn("Password hash mismatch - session invalid");
                clearSession();
                return null;
            }
            
            logger.info("Loaded session: {} (ID: {})", username, userId);
            return new SessionData(userId, username);
            
        } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.error("Failed to load session: {}", e.getMessage());
            clearSession();
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
