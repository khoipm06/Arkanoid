package com.arkanoid.database.entity;

import java.time.LocalDateTime;

/**
 * Entity representing a user in the system
 */
public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public User(int id, String username, String passwordHash, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
