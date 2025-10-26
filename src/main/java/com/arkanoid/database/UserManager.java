package com.arkanoid.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Base64;

public class UserManager {
    
    public static class User {
        private int id;
        private String username;
        private String email;
        private LocalDateTime createdAt;
        private LocalDateTime lastLogin;

        public User(int id, String username, String email, LocalDateTime createdAt, LocalDateTime lastLogin) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.createdAt = createdAt;
            this.lastLogin = lastLogin;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastLogin() { return lastLogin; }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static User register(String username, String password, String email) {
        String sql = "INSERT INTO users (username, password, email, created_at) VALUES (?, ?, ?, ?)";
        String createProfileSql = "INSERT INTO player_profiles (user_id) VALUES (?)";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.setString(3, email);
            pstmt.setString(4, LocalDateTime.now().toString());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        
                        try (PreparedStatement profileStmt = DatabaseManager.getConnection().prepareStatement(createProfileSql)) {
                            profileStmt.setInt(1, userId);
                            profileStmt.executeUpdate();
                        }
                        
                        return new User(userId, username, email, LocalDateTime.now(), null);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User login(String username, String password) {
        String sql = "SELECT id, username, email, created_at, last_login FROM users WHERE username = ? AND password = ?";
        String updateLoginSql = "UPDATE users SET last_login = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String email = rs.getString("email");
                    LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                    String lastLoginStr = rs.getString("last_login");
                    LocalDateTime lastLogin = lastLoginStr != null ? LocalDateTime.parse(lastLoginStr) : null;
                    
                    try (PreparedStatement updateStmt = DatabaseManager.getConnection().prepareStatement(updateLoginSql)) {
                        updateStmt.setString(1, LocalDateTime.now().toString());
                        updateStmt.setInt(2, id);
                        updateStmt.executeUpdate();
                    }
                    
                    return new User(id, username, email, createdAt, lastLogin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
