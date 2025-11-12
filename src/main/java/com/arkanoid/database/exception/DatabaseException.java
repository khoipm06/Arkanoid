package com.arkanoid.database.exception;

/**
 * Base exception for all database-related errors
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
