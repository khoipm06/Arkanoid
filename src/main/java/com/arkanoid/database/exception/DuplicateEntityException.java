package com.arkanoid.database.exception;

/**
 * Exception thrown when a duplicate entity is detected
 */
public class DuplicateEntityException extends DatabaseException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
