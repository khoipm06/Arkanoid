package com.arkanoid.database;

import com.arkanoid.database.exception.DatabaseException;
import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Thread-safe DatabaseManager with bounded SQLite connection pooling, WAL mode,
 * transaction support, and clean lifecycle management.
 */
public class DatabaseManager implements AutoCloseable {
    private static final Logger logger = GameLogger.getLogger(DatabaseManager.class);
    private static volatile DatabaseManager instance;
    private static final String DB_URL = "jdbc:sqlite:data/arkanoid.db";
    private static final int POOL_SIZE = 5;

    private final BlockingQueue<Connection> connectionPool;
    private final Set<Connection> leasedConnections;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private volatile boolean initialized = false;

    private DatabaseManager() {
        this.connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);
        this.leasedConnections = ConcurrentHashMap.newKeySet();
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
            String dbPath = DB_URL.replaceFirst("^jdbc:sqlite:", "");
            File dbFile = new File(dbPath);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            connectionPool.clear();
            leasedConnections.clear();
            activeConnections.set(0);

            for (int i = 0; i < POOL_SIZE; i++) {
                connectionPool.offer(createConfiguredConnection(i == 0));
            }

            initialized = true;
            Connection conn = getConnection();
            try {
                createTables(conn);
            } finally {
                releaseConnection(conn);
            }

            logger.info("Database initialized successfully with WAL connection pool (size={})", POOL_SIZE);
        } catch (SQLException e) {
            initialized = false;
            closeAllConnections();
            throw new DatabaseException("Failed to initialize database", e);
        }
    }

    private Connection createConfiguredConnection(boolean configureWalMode) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(true);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA busy_timeout=5000;");
            if (configureWalMode) {
                stmt.execute("PRAGMA journal_mode=WAL;");
            }
            stmt.execute("PRAGMA synchronous=NORMAL;");
        }
        return conn;
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
     * Get a connection from the pool, or an overflow connection if exhausted.
     * Caller must return it via {@link #releaseConnection(Connection)}.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = connectionPool.poll();
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    leasedConnections.add(conn);
                    activeConnections.incrementAndGet();
                    return conn;
                }
            } catch (SQLException e) {
                logger.warn("Pooled connection check failed, creating replacement: {}", e.getMessage());
            }
        }

        logger.warn("Connection pool exhausted or empty ({}/{} active), creating overflow connection",
                activeConnections.get(), POOL_SIZE);
        Connection overflow = createConfiguredConnection(false);
        leasedConnections.add(overflow);
        activeConnections.incrementAndGet();
        return overflow;
    }

    /**
     * Release a connection back to the pool, closing overflow connections when the pool is full.
     */
    public void releaseConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        leasedConnections.remove(conn);
        activeConnections.updateAndGet(n -> Math.max(0, n - 1));

        try {
            if (conn.isClosed()) {
                return;
            }
            if (!conn.getAutoCommit()) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.warn("Error resetting connection state on release: {}", e.getMessage());
            closeQuietly(conn);
            return;
        }

        if (!initialized || !connectionPool.offer(conn)) {
            closeQuietly(conn);
            logger.debug("Overflow or post-close connection closed");
        }
    }

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

    public void executeUpdate(Consumer<Connection> operation) {
        executeInTransaction(conn -> {
            operation.accept(conn);
            return null;
        });
    }

    public String getPoolStats() {
        long activeCount = getActiveConnectionCount();
        return String.format("Connection Pool: %d/%d active, %d available",
                activeCount, POOL_SIZE, connectionPool.size());
    }

    public long getActiveConnectionCount() {
        return activeConnections.get();
    }

    @Override
    public synchronized void close() {
        if (!initialized && connectionPool.isEmpty() && leasedConnections.isEmpty()) {
            return;
        }
        logger.info("Closing database connection pool - {}", getPoolStats());
        initialized = false;
        closeAllConnections();
        activeConnections.set(0);
        logger.info("Database connections closed");
    }

    private void closeAllConnections() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            closeQuietly(conn);
        }
        for (Connection leased : leasedConnections) {
            closeQuietly(leased);
        }
        leasedConnections.clear();
    }

    private void closeQuietly(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("Error closing connection: {}", e.getMessage());
        }
    }
}
