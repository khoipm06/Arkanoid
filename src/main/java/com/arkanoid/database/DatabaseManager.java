package com.arkanoid.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data/arkanoid.db";
    private static Connection connection;

    public static void initialize() {
        try {
            new java.io.File("data").mkdirs();
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables() throws SQLException {
        String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        email TEXT,
                        created_at TEXT NOT NULL,
                        last_login TEXT
                    )
                """;

        String createPlayerProfilesTable = """
                    CREATE TABLE IF NOT EXISTS player_profiles (
                        user_id INTEGER PRIMARY KEY,
                        coins INTEGER DEFAULT 0,
                        high_score INTEGER DEFAULT 0,
                        current_skin TEXT DEFAULT 'default',
                        games_played INTEGER DEFAULT 0,
                        total_score INTEGER DEFAULT 0,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                """;

        String createInventoryTable = """
                    CREATE TABLE IF NOT EXISTS inventory (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        item_id TEXT NOT NULL,
                        quantity INTEGER DEFAULT 1,
                        purchased_at TEXT NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                """;

        String createGameHistoryTable = """
                    CREATE TABLE IF NOT EXISTS game_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        score INTEGER NOT NULL,
                        level_reached INTEGER NOT NULL,
                        duration_seconds INTEGER NOT NULL,
                        played_at TEXT NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createPlayerProfilesTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createGameHistoryTable);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
