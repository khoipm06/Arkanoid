package com.arkanoid.systems.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Collection;
import java.util.Map;

/**
 * Centralized logging utility for the Arkanoid game.
 * 
 * Usage pattern - Each class should create its own logger instance:
 * <pre>
 * private static final Logger logger = GameLogger.getLogger(MyClass.class);
 * logger.info("Message with {} parameter", value);
 * </pre>
 * 
 * This class provides:
 * - Logger factory methods (getLogger)
 * - MDC (Mapped Diagnostic Context) utilities for thread-local context
 * - Convenience methods for logging collection states and thread info
 */
public class GameLogger {
    
    private GameLogger() {
        // Utility class - no instantiation
    }

    /**
     * Get a logger for the specified class.
     * This is the recommended way to obtain loggers.
     * 
     * @param clazz The class to create a logger for
     * @return SLF4J Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Get a logger with the specified name.
     * Use this for loggers not tied to a specific class.
     * 
     * @param name The logger name
     * @return SLF4J Logger instance
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * Log the state of a collection at TRACE level.
     * Useful for debugging collection size changes.
     * 
     * @param logger The logger to use
     * @param collectionName Name of the collection
     * @param collection The collection to log
     */
    public static void logCollectionState(Logger logger, String collectionName, Collection<?> collection) {
        if (logger.isTraceEnabled()) {
            logger.trace("COLLECTION_STATE: {}={} items", collectionName, collection != null ? collection.size() : 0);
        }
    }
    
    /**
     * Log thread information at DEBUG level.
     * Useful for debugging concurrency issues.
     * 
     * @param logger The logger to use
     * @param operation Description of the operation
     */
    public static void logThreadInfo(Logger logger, String operation) {
        if (logger.isDebugEnabled()) {
            logger.debug("THREAD_INFO: operation={}, thread={}", operation, Thread.currentThread().getName());
        }
    }

    /**
     * Set a thread-local context value for MDC.
     * This value will appear in all log messages from this thread
     * until cleared or overwritten.
     * 
     * @param key Context key
     * @param value Context value
     */
    public static void setThreadContext(String key, Object value) {
        MDC.put(key, String.valueOf(value));
    }

    /**
     * Clear all thread-local context values.
     * Should be called when thread work is complete to avoid memory leaks.
     */
    public static void clearThreadContext() {
        MDC.clear();
    }

    /**
     * Get a copy of the current thread's MDC context.
     * 
     * @return Map of context key-value pairs
     */
    public static Map<String, String> getThreadContext() {
        return MDC.getCopyOfContextMap();
    }
}
