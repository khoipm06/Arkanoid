package com.arkanoid.database;

import com.arkanoid.database.exception.DatabaseException;
import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Improved DatabaseManager with connection pooling, transaction support, and
 * proper lifecycle management.
 * Singleton pattern with thread-safe initialization.
 */
public class DatabaseManager implements AutoCloseable {
    private static final Logger logger = GameLogger.getLogger(DatabaseManager.class);
    private static volatile DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:data/arkanoid.db";
    private static final int POOL_SIZE = 5;

    private final List<Connection> connectionPool;
    private final List<Boolean> connectionStatus;
    private boolean initialized = false;

    private DatabaseManager() {
        this.connectionPool = new ArrayList<>(POOL_SIZE);
        this.connectionStatus = new ArrayList<>(POOL_SIZE);
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Ensure directory exists
            String dbPath = DB_URL.replaceFirst("^jdbc:sqlite:", "");
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Initialize connection pool
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(DB_URL);
                conn.setAutoCommit(true);
                connectionPool.add(conn);
                connectionStatus.add(false); // false = available
            }

            // Create tables using a connection from the pool
            try (Connection conn = getConnection()) {
                createTables(conn);
            }

            initialized = true;
            logger.info("Database initialized successfully with connection pool");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database", e);
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        last_login TEXT
                    )
                """;

        String createPlayerProfilesTable = """
                    CREATE TABLE IF NOT EXISTS player_profiles (
                        user_id INTEGER PRIMARY KEY,
                        money INTEGER DEFAULT 0,
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

        String createGameSavesTable = """
                    CREATE TABLE IF NOT EXISTS game_saves (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        save_name TEXT NOT NULL,
                        level_number INTEGER NOT NULL,
                        score INTEGER NOT NULL,
                        lives INTEGER NOT NULL,
                        elapsed_time_seconds INTEGER NOT NULL,
                        game_state_json BLOB NOT NULL,
                        thumbnail_data BLOB,
                        created_at TEXT NOT NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                """;

        String createUserPreferencesTable = """
                    CREATE TABLE IF NOT EXISTS user_preferences (
                        user_id INTEGER PRIMARY KEY,
                        music_volume INTEGER DEFAULT 50,
                        FOREIGN KEY (user_id) REFERENCES users(id)
                    )
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createPlayerProfilesTable);
            stmt.execute(createInventoryTable);
            stmt.execute(createGameSavesTable);
            stmt.execute(createUserPreferencesTable);
        }
    }

    /**
     * Get a connection from the pool. Caller must return it via releaseConnection()
     * or use executeInTransaction/executeQuery which handle it automatically.
     */
    public synchronized Connection getConnection() throws SQLException {
        for (int i = 0; i < POOL_SIZE; i++) {
            if (!connectionStatus.get(i)) {
                connectionStatus.set(i, true);
                logger.debug("Connection #{} acquired from pool", i);
                return connectionPool.get(i);
            }
        }
        // If pool exhausted, create a temporary connection
        long activeCount = getActiveConnectionCount();
        logger.warn("Connection pool exhausted ({}/{} active), creating temporary connection", 
                    activeCount, POOL_SIZE);
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Release a connection back to the pool
     */
    public synchronized void releaseConnection(Connection conn) {
        int index = connectionPool.indexOf(conn);
        if (index >= 0) {
            connectionStatus.set(index, false);
            logger.debug("Connection #{} released back to pool", index);
        } else {
            // Temporary connection, close it
            try {
                conn.close();
                logger.debug("Temporary connection closed");
            } catch (SQLException e) {
                logger.error("Error closing temporary connection: {}", e.getMessage());
            }
        }
    }

    /**
     * Execute code within a transaction. Automatically commits on success, rolls
     * back on exception.
     */
    public <T> T executeInTransaction(Function<Connection, T> operation) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            T result = operation.apply(conn);

            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Error rolling back transaction: {}", rollbackEx.getMessage());
                }
            }
            throw new DatabaseException("Transaction failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit: {}", e.getMessage());
                }
                releaseConnection(conn);
            }
        }
    }

    /**
     * Execute a query operation (read-only, no transaction needed)
     */
    public <T> T executeQuery(Function<Connection, T> operation) {
        Connection conn = null;
        try {
            conn = getConnection();
            return operation.apply(conn);
        } catch (SQLException e) {
            throw new DatabaseException("Query failed", e);
        } finally {
            if (conn != null) {
                releaseConnection(conn);
            }
        }
    }

    /**
     * Execute an update operation within a transaction
     */
    public void executeUpdate(Consumer<Connection> operation) {
        executeInTransaction(conn -> {
            operation.accept(conn);
            return null;
        });
    }

    /**
     * Get connection pool statistics for monitoring
     */
    public synchronized String getPoolStats() {
        long activeCount = getActiveConnectionCount();
        return String.format("Connection Pool: %d/%d active, %d available", 
                           activeCount, POOL_SIZE, POOL_SIZE - activeCount);
    }

    /**
     * Get number of active connections
     */
    public synchronized long getActiveConnectionCount() {
        return connectionStatus.stream().filter(status -> status).count();
    }

    @Override
    public void close() {
        logger.info("Closing database connection pool - {}", getPoolStats());
        for (Connection conn : connectionPool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing connection: {}", e.getMessage());
            }
        }
        connectionPool.clear();
        connectionStatus.clear();
        initialized = false;
        logger.info("Database connections closed");
    }
}
