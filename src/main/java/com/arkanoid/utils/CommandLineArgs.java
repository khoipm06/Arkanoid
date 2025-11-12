package com.arkanoid.utils;

import ch.qos.logback.classic.Level;
import com.arkanoid.systems.logging.LoggingConfig;


public class CommandLineArgs {
    
    public static class Config {
        private final LoggingConfig loggingConfig;
        
        public Config() {
            this.loggingConfig = new LoggingConfig();
        }
        
        public LoggingConfig getLoggingConfig() {
            return loggingConfig;
        }
        
        public void applyEnvironmentVariables() {
            loggingConfig.applyEnvironmentVariables();
        }
    }

    public static Config parse(String[] args) {
        Config config = new Config();
        LoggingConfig loggingConfig = config.getLoggingConfig();
        
        for (String arg : args) {
            if (arg.startsWith("--log-level=")) {
                String levelStr = arg.substring("--log-level=".length());
                try {
                    loggingConfig.setLogLevel(Level.toLevel(levelStr, Level.INFO));
                } catch (Exception e) {
                    System.err.println("Invalid log level: " + levelStr);
                }
            } else if (arg.startsWith("--log-to-console=")) {
                String value = arg.substring("--log-to-console=".length());
                loggingConfig.setConsoleEnabled(Boolean.parseBoolean(value));
            } else if (arg.startsWith("--log-to-file=")) {
                String value = arg.substring("--log-to-file=".length());
                loggingConfig.setFileEnabled(Boolean.parseBoolean(value));
            } else if (arg.startsWith("--log-file-path=")) {
                String value = arg.substring("--log-file-path=".length());
                loggingConfig.setLogFilePath(value);
            }
        }
        
        return config;
    }
    
    public static void printUsage() {
        System.out.println("Usage: java -jar arkanoid.jar [options]");
        System.out.println("Options:");
        System.out.println("  --log-level=LEVEL       Set log level (TRACE|DEBUG|INFO|WARN|ERROR)");
        System.out.println("  --log-to-console=BOOL   Enable/disable console logging");
        System.out.println("  --log-to-file=BOOL      Enable/disable file logging");
        System.out.println("  --log-file-path=PATH    Set log file path");
        System.out.println();
        System.out.println("Environment Variables:");
        System.out.println("  LOG_LEVEL               Override log level");
        System.out.println("  LOG_TO_CONSOLE          Override console logging");
        System.out.println("  LOG_TO_FILE             Override file logging");
        System.out.println("  LOG_FILE_PATH           Override log file path");
    }
}
