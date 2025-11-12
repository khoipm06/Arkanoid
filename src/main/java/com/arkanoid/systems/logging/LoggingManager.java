package com.arkanoid.systems.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LoggingManager {
    private static final org.slf4j.Logger logger = GameLogger.getLogger(LoggingManager.class);
    private static final LoggingManager instance = new LoggingManager();
    private LoggingConfig config;
    private boolean initialized = false;
    
    private LoggingManager() {
    }
    
    public static LoggingManager getInstance() {
        return instance;
    }
    
    /**
     * Initialize the logging system with the given configuration.
     */
    public synchronized void initialize(LoggingConfig config) {
        if (initialized) {
            logger.warn("LoggingManager already initialized");
            return;
        }
        
        this.config = config;
        
        // Configure Logback programmatically
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(ROOT_LOGGER_NAME);
        rootLogger.setLevel(config.getLogLevel());

        System.setProperty("LOG_LEVEL", config.getLogLevel().toString());
        
        initialized = true;
        logger.info("LoggingManager initialized with level: {}", config.getLogLevel());
    }
    
    /**
     * Shutdown the logging system gracefully.
     */
    public synchronized void shutdown() {
        if (!initialized) {
            return;
        }
        
        logger.info("LoggingManager shutting down...");
        
        // Stop Logback and flush all appenders
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
        
        initialized = false;
    }
    
    /**
     * Wait for all pending log events to be processed.
     */
    public void awaitTermination(long timeout, TimeUnit unit) {
        // Logback AsyncAppender handles this internally
        try {
            Thread.sleep(unit.toMillis(timeout));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Change log level at runtime.
     */
    public void setLogLevel(Level level) {
        if (config != null) {
            config.setLogLevel(level);
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(ROOT_LOGGER_NAME);
            rootLogger.setLevel(level);
            logger.info("Log level changed to: {}", level);
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public LoggingConfig getConfig() {
        return config;
    }
}
