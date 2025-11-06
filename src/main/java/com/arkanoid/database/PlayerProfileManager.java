package com.arkanoid.database;

import java.sql.*;

public class PlayerProfileManager {
    
    public static class ProfileData {
        public int coins;
        public int highScore;
        public String currentSkin;
        public int gamesPlayed;
        public int totalScore;

        public ProfileData(int coins, int highScore, String currentSkin, int gamesPlayed, int totalScore) {
            this.coins = coins;
            this.highScore = highScore;
            this.currentSkin = currentSkin;
            this.gamesPlayed = gamesPlayed;
            this.totalScore = totalScore;
        }
    }

    public static ProfileData getProfile(int userId) {
        String sql = "SELECT * FROM player_profiles WHERE user_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ProfileData(
                        rs.getInt("coins"),
                        rs.getInt("high_score"),
                        rs.getString("current_skin"),
                        rs.getInt("games_played"),
                        rs.getInt("total_score")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateCoins(int userId, int coins) {
        String sql = "UPDATE player_profiles SET coins = ? WHERE user_id = ?";
        executeUpdate(sql, coins, userId);
    }

    public static void updateHighScore(int userId, int highScore) {
        String sql = "UPDATE player_profiles SET high_score = ? WHERE user_id = ? AND high_score < ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, highScore);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, highScore);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
