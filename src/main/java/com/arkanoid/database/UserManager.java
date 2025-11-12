package com.arkanoid.database;

import java.sql.*;
import java.time.LocalDateTime;

public class UserManager {
    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();

    public static class User {
        private int id;
        private String username;
        private LocalDateTime createdAt;
        private LocalDateTime lastLogin;

        public User(int id, String username, LocalDateTime createdAt, LocalDateTime lastLogin) {
            this.id = id;
            this.username = username;
            this.createdAt = createdAt;
            this.lastLogin = lastLogin;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getLastLogin() {
            return lastLogin;
        }
    }

    public static User register(String username, String password) {
        String sql = "INSERT INTO users (username, password, created_at) VALUES (?, ?, ?)";
        String createProfileSql = "INSERT INTO player_profiles (user_id) VALUES (?)";

        if (usernameExists(username)) {
            return null; // User already exists
        }

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, LocalDateTime.now().toString());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int userId = rs.getInt(1);

                            // Create default profile
                            try (PreparedStatement profileStmt = conn.prepareStatement(createProfileSql)) {
                                profileStmt.setInt(1, userId);
                                profileStmt.executeUpdate();
                            }

                            return new User(userId, username, LocalDateTime.now(), null);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return null;
    }

    public static User login(String username, String password) {
        String sql = "SELECT id, username, created_at, last_login FROM users WHERE username = ? AND password = ?";
        String updateLoginSql = "UPDATE users SET last_login = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                        String lastLoginStr = rs.getString("last_login");
                        LocalDateTime lastLogin = lastLoginStr != null ? LocalDateTime.parse(lastLoginStr) : null;

                        // Update login time
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateLoginSql)) {
                            updateStmt.setString(1, LocalDateTime.now().toString());
                            updateStmt.setInt(2, id);
                            updateStmt.executeUpdate();
                        }

                        return new User(id, username, createdAt, lastLogin);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return null;
    }

    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return false;
    }
}
