package com.arkanoid.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerProfileManager {
    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();

    public static class ProfileData {
        public int money;
        public int highScore;
        public String currentSkin;
        public int gamesPlayed;
        public int totalScore;

        public ProfileData(int money, int highScore, String currentSkin, int gamesPlayed, int totalScore) {
            this.money = money;
            this.highScore = highScore;
            this.currentSkin = currentSkin;
            this.gamesPlayed = gamesPlayed;
            this.totalScore = totalScore;
        }
    }

    public static class LeaderboardEntry {
        public final String username;
        public final int highScore;

        public LeaderboardEntry(String username, int highScore) {
            this.username = username;
            this.highScore = highScore;
        }
    }

    public static List<LeaderboardEntry> getLeaderboardData() {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        String sql = """
                    SELECT u.username, pp.high_score
                    FROM users u
                    JOIN player_profiles pp ON u.id = pp.user_id
                    ORDER BY pp.high_score DESC
                """;

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    leaderboard.add(new LeaderboardEntry(
                            rs.getString("username"),
                            rs.getInt("high_score")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return leaderboard;
    }

    public static ProfileData getProfile(int userId) {
        String sql = "SELECT * FROM player_profiles WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new ProfileData(
                                rs.getInt("money"),
                                rs.getInt("high_score"),
                                rs.getString("current_skin"),
                                rs.getInt("games_played"),
                                rs.getInt("total_score"));
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

    public static void updateMoney(int userId, int money) {
        String sql = "UPDATE player_profiles SET money = ? WHERE user_id = ?";
        executeUpdate(sql, money, userId);
    }

    public static void updateHighScore(int userId, int highScore) {
        String sql = "UPDATE player_profiles SET high_score = ? WHERE user_id = ? AND high_score < ?";
        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, highScore);
                pstmt.setInt(2, userId);
                pstmt.setInt(3, highScore);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
    }

    public static void updateSkin(int userId, String skin) {
        String sql = "UPDATE player_profiles SET current_skin = ? WHERE user_id = ?";
        executeUpdate(sql, skin, userId);
    }

    public static void incrementGamesPlayed(int userId) {
        String sql = "UPDATE player_profiles SET games_played = games_played + 1 WHERE user_id = ?";
        executeUpdate(sql, userId);
    }

    public static void addToTotalScore(int userId, int scoreToAdd) {
        String sql = "UPDATE player_profiles SET total_score = total_score + ? WHERE user_id = ?";
        executeUpdate(sql, scoreToAdd, userId);
    }

    private static void executeUpdate(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
    }
}
