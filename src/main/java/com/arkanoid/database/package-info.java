/**
 * SQLite persistence layer, WAL connection pooling, repositories, and player data managers.
 *
 * <p>{@link com.arkanoid.database.DatabaseManager} maintains a bounded connection pool configured
 * with SQLite Write-Ahead Logging ({@code WAL}), {@code busy_timeout}, and explicit transaction
 * boundaries, exposed to application services through
 * {@link com.arkanoid.database.RepositoryFactory}.
 *
 * <p><strong>Thread-safety boundary:</strong> {@link com.arkanoid.database.DatabaseManager} and
 * its repository implementations are thread-safe and designed to be invoked from
 * {@link com.arkanoid.systems.threading.ThreadManager} background I/O threads so disk writes,
 * LZ4 save-slot persistence, and profile updates never block the JavaFX Application Thread.
 */
package com.arkanoid.database;
