package com.arkanoid.database.repository.impl;

import com.arkanoid.database.DatabaseManager;
import com.arkanoid.database.entity.PlayerProfile;
import com.arkanoid.database.exception.DatabaseException;
import com.arkanoid.database.repository.PlayerProfileRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of PlayerProfileRepository
 */
public class PlayerProfileRepositoryImpl implements PlayerProfileRepository {
    private final DatabaseManager databaseManager;

    public PlayerProfileRepositoryImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public PlayerProfile create(int userId) {
        String sql = "INSERT INTO player_profiles (user_id, money, high_score, current_skin, games_played, total_score) "
                +
                "VALUES (?, 0, 0, 'default', 0, 0)";

        databaseManager.executeUpdate(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseException("Failed to create player profile", e);
            }
        });

        return new PlayerProfile(userId, 0, 0, "default", 0, 0);
    }

    @Override
    public Optional<PlayerProfile> findByUserId(int userId) {
        String sql = "SELECT * FROM player_profiles WHERE user_id = ?";

        return databaseManager.executeQuery(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToProfile(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to find player profile", e);
            }
        });
    }

    @Override
    public void update(PlayerProfile profile) {
        String sql = "UPDATE player_profiles SET money = ?, high_score = ?, current_skin = ?, " +
                "games_played = ?, total_score = ? WHERE user_id = ?";

        databaseManager.executeUpdate(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, profile.getMoney());
                pstmt.setInt(2, profile.getHighScore());
                pstmt.setString(3, profile.getCurrentSkin());
                pstmt.setInt(4, profile.getGamesPlayed());
                pstmt.setInt(5, profile.getTotalScore());
                pstmt.setInt(6, profile.getUserId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseException("Failed to update player profile", e);
            }
        });
    }

    @Override
    public List<PlayerProfile> getLeaderboard(int limit) {
        String sql = """
                SELECT pp.*, u.username
                FROM player_profiles pp
                JOIN users u ON pp.user_id = u.id
                ORDER BY pp.high_score DESC
                LIMIT ?
                """;

        return databaseManager.executeQuery(conn -> {
            List<PlayerProfile> leaderboard = new ArrayList<>();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, limit);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        leaderboard.add(mapResultSetToProfile(rs));
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseException("Failed to get leaderboard", e);
            }

            return leaderboard;
        });
    }

    private PlayerProfile mapResultSetToProfile(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        int money = rs.getInt("money");
        int highScore = rs.getInt("high_score");
        String currentSkin = rs.getString("current_skin");
        int gamesPlayed = rs.getInt("games_played");
        int totalScore = rs.getInt("total_score");

        return new PlayerProfile(userId, money, highScore, currentSkin, gamesPlayed, totalScore);
    }
}
