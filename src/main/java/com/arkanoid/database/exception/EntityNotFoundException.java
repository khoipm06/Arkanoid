package com.arkanoid.database.exception;

/**
 * Exception thrown when a database entity is not found
 */
public class EntityNotFoundException extends DatabaseException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
