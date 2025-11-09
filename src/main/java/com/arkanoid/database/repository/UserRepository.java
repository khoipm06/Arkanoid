package com.arkanoid.database.repository;

import com.arkanoid.database.entity.User;
import java.util.Optional;

/**
 * Repository interface for User data access
 */
public interface UserRepository {
    /**
     * Create a new user
     * 
     * @param username     the username
     * @param passwordHash the hashed password
     * @return the created user
     */
    User create(String username, String passwordHash);

    /**
     * Find a user by ID
     * 
     * @param id the user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(int id);

    /**
     * Find a user by username
     * 
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a username already exists
     * 
     * @param username the username to check
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Update user's last login time
     * 
     * @param userId the user ID
     */
    void updateLastLogin(int userId);
}
