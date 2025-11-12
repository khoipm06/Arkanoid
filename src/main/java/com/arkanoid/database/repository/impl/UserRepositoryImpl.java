package com.arkanoid.database.repository.impl;

import com.arkanoid.database.DatabaseManager;
import com.arkanoid.database.entity.User;
import com.arkanoid.database.exception.DatabaseException;
import com.arkanoid.database.exception.DuplicateEntityException;
import com.arkanoid.database.repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SQLite implementation of UserRepository
 */
public class UserRepositoryImpl implements UserRepository {
    private final DatabaseManager databaseManager;

    public UserRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public User create(String username, String passwordHash) {
        if (existsByUsername(username)) {
            throw new DuplicateEntityException("Username already exists: " + username);
        }

        String sql = "INSERT INTO users (username, password, created_at) VALUES (?, ?, ?)";

        return databaseManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                LocalDateTime now = LocalDateTime.now();
                pstmt.setString(1, username);
                pstmt.setString(2, passwordHash);
                pstmt.setString(3, now.toString());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DatabaseException("Creating user failed, no rows affected");
                }

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        return new User(userId, username, passwordHash, now, null);
                    } else {
                        throw new DatabaseException("Creating user failed, no ID obtained");
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to create user", e);
            }
        });
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        return databaseManager.executeQuery(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to find user by ID", e);
            }
        });
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        return databaseManager.executeQuery(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to find user by username", e);
            }
        });
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        return databaseManager.executeQuery(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to check username existence", e);
            }
        });
    }

    @Override
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";

        databaseManager.executeUpdate(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, LocalDateTime.now().toString());
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseException("Failed to update last login", e);
            }
        });
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
        String lastLoginStr = rs.getString("last_login");
        LocalDateTime lastLogin = lastLoginStr != null ? LocalDateTime.parse(lastLoginStr) : null;

        return new User(id, username, passwordHash, createdAt, lastLogin);
    }
}
