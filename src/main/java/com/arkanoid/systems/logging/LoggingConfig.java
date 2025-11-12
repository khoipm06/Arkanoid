package com.arkanoid.systems.logging;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;

public class LoggingConfig {
    private static final Logger logger = GameLogger.getLogger(LoggingConfig.class);
    private Level logLevel = Level.INFO;
    private boolean consoleEnabled = true;
    private boolean fileEnabled = true;
    private String logFilePath = "logs/arkanoid.log";
    private String logPattern = "[%date{ISO8601}] [%level] [%thread] [%logger{36}.%method:%line] - %msg%n";
    
    public LoggingConfig() {
    }
    
    public Level getLogLevel() {
        return logLevel;
    }
    
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }
    
    public boolean isConsoleEnabled() {
        return consoleEnabled;
    }
    
    public void setConsoleEnabled(boolean consoleEnabled) {
        this.consoleEnabled = consoleEnabled;
    }
    
    public boolean isFileEnabled() {
        return fileEnabled;
    }
    
    public void setFileEnabled(boolean fileEnabled) {
        this.fileEnabled = fileEnabled;
    }
    
    public String getLogFilePath() {
        return logFilePath;
    }
    
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }
    
    public String getLogPattern() {
        return logPattern;
    }
    
    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }
    
    /**
     * Apply environment variables to override configuration.
     */
    public void applyEnvironmentVariables() {
        String envLogLevel = System.getenv("LOG_LEVEL");
        if (envLogLevel != null && !envLogLevel.isEmpty()) {
            try {
                this.logLevel = Level.toLevel(envLogLevel, Level.INFO);
            } catch (Exception e) {
                logger.warn("Invalid LOG_LEVEL: {}", envLogLevel);
            }
        }
        
        String envConsole = System.getenv("LOG_TO_CONSOLE");
        if (envConsole != null) {
            this.consoleEnabled = Boolean.parseBoolean(envConsole);
        }
        
        String envFile = System.getenv("LOG_TO_FILE");
        if (envFile != null) {
            this.fileEnabled = Boolean.parseBoolean(envFile);
        }
        
        String envFilePath = System.getenv("LOG_FILE_PATH");
        if (envFilePath != null && !envFilePath.isEmpty()) {
            this.logFilePath = envFilePath;
        }
    }
}
