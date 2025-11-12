package com.arkanoid.systems.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameLogger {
    private static final Map<Class<?>, Logger> loggerCache = new ConcurrentHashMap<>();
    
    private GameLogger() {
    }

    public static void trace(String message, Object... args) {
        getCallerLogger().trace(message, args);
    }
    
    public static void debug(String message, Object... args) {
        getCallerLogger().debug(message, args);
    }
    
    public static void info(String message, Object... args) {
        getCallerLogger().info(message, args);
    }
    
    public static void warn(String message, Object... args) {
        getCallerLogger().warn(message, args);
    }
    
    public static void error(String message, Object... args) {
        getCallerLogger().error(message, args);
    }
    
    public static void error(String message, Throwable throwable) {
        getCallerLogger().error(message, throwable);
    }
    
    public static void error(String message, Throwable throwable, Object... args) {
        getCallerLogger().error(String.format(message.replace("{}", "%s"), args), throwable);
    }

    public static void logCollectionState(String collectionName, Collection<?> collection) {
        if (isTraceEnabled()) {
            trace("COLLECTION_STATE: {}={} items", collectionName, collection != null ? collection.size() : 0);
        }
    }
    
    public static void logThreadInfo(String operation) {
        if (isDebugEnabled()) {
            debug("THREAD_INFO: operation={}, thread={}", operation, Thread.currentThread().getName());
        }
    }

    public static void setThreadContext(String key, Object value) {
        MDC.put(key, String.valueOf(value));
    }

    public static void clearThreadContext() {
        MDC.clear();
    }

    public static Map<String, String> getThreadContext() {
        return MDC.getCopyOfContextMap();
    }

    public static boolean isTraceEnabled() {
        return getCallerLogger().isTraceEnabled();
    }
    
    public static boolean isDebugEnabled() {
        return getCallerLogger().isDebugEnabled();
    }

    public static Logger getLogger(Class<?> clazz) {
        return loggerCache.computeIfAbsent(clazz, LoggerFactory::getLogger);
    }
    
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    private static Logger getCallerLogger() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            String callerClassName = stackTrace[3].getClassName();
            try {
                Class<?> callerClass = Class.forName(callerClassName);
                return getLogger(callerClass);
            } catch (ClassNotFoundException e) {
                return getLogger(callerClassName);
            }
        }
        return getLogger(GameLogger.class);
    }
}
