package com.arkanoid.database;

import com.arkanoid.database.exception.EntityNotFoundException;
import com.arkanoid.database.entity.User;
import com.arkanoid.database.entity.PlayerProfile;
import com.arkanoid.database.repository.PlayerProfileRepository;
import com.arkanoid.database.repository.UserRepository;
import com.arkanoid.utils.PasswordHasher;

import java.util.Optional;

/**
 * Legacy UserManager that now delegates to AuthenticationService. Kept for
 * backward compatibility with existing UI controllers.
 */
public class UserManager {
    private static final PasswordHasher passwordHasher = RepositoryFactory.getInstance().getPasswordHasher();
    private static final PlayerProfileRepository profileRepository = RepositoryFactory.getInstance().getPlayerProfileRepository();
    private static final UserRepository userRepository = RepositoryFactory.getInstance().getUserRepository();

    /**
     * Register a new user.
     * 
     * @param username the username
     * @param password the plain text password (will be hashed)
     * @return User object if successful, null if username already exists
     */
    public static User register(String username, String password) {
        try {
            String passwordHash = passwordHasher.hashPassword(password);
            User user = userRepository.create(username, passwordHash);

            // Create default profile for new user
            profileRepository.create(user.getId());

            return user;
        } catch (Exception e) {
            // Username already exists or other error
            return null;
        }
    }

    /**
     * Login a user.
     * 
     * @param username the username
     * @param password the plain text password
     * @return User object if successful, null if credentials are invalid
     */
    public static User login(String username, String password) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                throw new EntityNotFoundException("User not found: " + username);
            }

            User user = userOpt.get();
            if (!passwordHasher.verifyPassword(password, user.getPasswordHash())) {
                throw new EntityNotFoundException("Invalid credentials");
            }

            userRepository.updateLastLogin(user.getId());
            return user;
        } catch (Exception e) {
            // Invalid credentials or user not found
            return null;
        }
    }

    /**
     * Check if a username already exists.
     * 
     * @param username the username to check
     * @return true if exists, false otherwise
     */
    public static boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Get user profile
     * 
     * @param userId the user ID
     * @return the player profile
     * @throws EntityNotFoundException if profile not found
     */
    public PlayerProfile getProfile(int userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user: " + userId));
    }
}
