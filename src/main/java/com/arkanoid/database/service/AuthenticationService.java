package com.arkanoid.database.service;

import com.arkanoid.database.entity.User;
import com.arkanoid.database.entity.PlayerProfile;
import com.arkanoid.database.exception.EntityNotFoundException;
import com.arkanoid.database.repository.UserRepository;
import com.arkanoid.database.repository.PlayerProfileRepository;

import java.util.Optional;

/**
 * Service for user authentication and management
 */
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PlayerProfileRepository profileRepository;
    private final PasswordService passwordService;

    public AuthenticationService(UserRepository userRepository,
            PlayerProfileRepository profileRepository,
            PasswordService passwordService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordService = passwordService;
    }

    /**
     * Register a new user with a profile
     * 
     * @param username the username
     * @param password plain text password
     * @return the created user
     */
    public User register(String username, String password) {
        String passwordHash = passwordService.hashPassword(password);
        User user = userRepository.create(username, passwordHash);

        // Create default profile for new user
        profileRepository.create(user.getId());

        return user;
    }

    /**
     * Authenticate a user
     * 
     * @param username the username
     * @param password plain text password
     * @return the authenticated user
     * @throws EntityNotFoundException if user not found or password incorrect
     */
    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + username);
        }

        User user = userOpt.get();
        if (!passwordService.verifyPassword(password, user.getPasswordHash())) {
            throw new EntityNotFoundException("Invalid credentials");
        }

        // Update last login time
        userRepository.updateLastLogin(user.getId());

        return user;
    }

    /**
     * Check if a username is available
     * 
     * @param username the username to check
     * @return true if available (not taken)
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Get user with profile
     * 
     * @param userId the user ID
     * @return the user
     * @throws EntityNotFoundException if user not found
     */
    public User getUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
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
