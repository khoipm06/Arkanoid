package com.arkanoid.database.repository.impl;

import com.arkanoid.database.DatabaseManager;
import com.arkanoid.database.entity.GameSave;
import com.arkanoid.database.exception.DatabaseException;
import com.arkanoid.database.repository.GameSaveRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of GameSaveRepository
 */
public class GameSaveRepositoryImpl implements GameSaveRepository {
    private final DatabaseManager dbManager;

    public GameSaveRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public GameSave create(int userId, String saveName, int levelNumber, int score, int lives,
            int elapsedTimeSeconds, String gameStateJson, byte[] thumbnailData) {
        String sql = """
                INSERT INTO game_saves (user_id, save_name, level_number, score, lives,
                                      elapsed_time_seconds, game_state_json, thumbnail_data, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return dbManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                LocalDateTime now = LocalDateTime.now();
                pstmt.setInt(1, userId);
                pstmt.setString(2, saveName);
                pstmt.setInt(3, levelNumber);
                pstmt.setInt(4, score);
                pstmt.setInt(5, lives);
                pstmt.setInt(6, elapsedTimeSeconds);
                pstmt.setString(7, gameStateJson);
                pstmt.setBytes(8, thumbnailData);
                pstmt.setString(9, now.toString());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DatabaseException("Creating game save failed, no rows affected");
                }

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int saveId = rs.getInt(1);
                        return new GameSave(saveId, userId, saveName, levelNumber, score, lives,
                                elapsedTimeSeconds, gameStateJson, thumbnailData, now);
                    } else {
                        throw new DatabaseException("Creating game save failed, no ID obtained");
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to create game save", e);
            }
        });
    }

    @Override
    public Optional<GameSave> findById(int id) {
        String sql = "SELECT * FROM game_saves WHERE id = ?";

        return dbManager.executeQuery(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToGameSave(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to find game save by ID", e);
            }
        });
    }

    @Override
    public List<GameSave> findByUserId(int userId) {
        String sql = "SELECT * FROM game_saves WHERE user_id = ? ORDER BY created_at DESC";

        return dbManager.executeQuery(conn -> {
            List<GameSave> saves = new ArrayList<>();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        saves.add(mapResultSetToGameSave(rs));
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to find game saves by user ID", e);
            }

            return saves;
        });
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM game_saves WHERE id = ?";

        return dbManager.executeInTransaction(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                throw new DatabaseException("Failed to delete game save", e);
            }
        });
    }

    @Override
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM game_saves WHERE user_id = ?";

        return dbManager.executeQuery(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to count game saves", e);
            }
        });
    }

    private GameSave mapResultSetToGameSave(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        String saveName = rs.getString("save_name");
        int levelNumber = rs.getInt("level_number");
        int score = rs.getInt("score");
        int lives = rs.getInt("lives");
        int elapsedTimeSeconds = rs.getInt("elapsed_time_seconds");
        String gameStateJson = rs.getString("game_state_json");
        byte[] thumbnailData = rs.getBytes("thumbnail_data");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));

        return new GameSave(id, userId, saveName, levelNumber, score, lives,
                elapsedTimeSeconds, gameStateJson, thumbnailData, createdAt);
    }
}
