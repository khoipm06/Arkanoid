package com.arkanoid.systems.threading;

import com.arkanoid.systems.logging.GameLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages thread-specific context and metadata for debugging and logging
 * purposes. Each thread can store custom key-value pairs that are automatically
 * included in logs.
 */
public class ThreadContext {
    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(ConcurrentHashMap::new);
    private static final Map<Long, ThreadMetadata> THREAD_METADATA = new ConcurrentHashMap<>();

    /**
     * Metadata associated with a specific thread.
     */
    public static class ThreadMetadata {
        private final long threadId;
        private final String threadName;
        private final String purpose;
        private final long createdAt;

        public ThreadMetadata(long threadId, String threadName, String purpose) {
            this.threadId = threadId;
            this.threadName = threadName;
            this.purpose = purpose;
            this.createdAt = System.currentTimeMillis();
        }

        public long getThreadId() {
            return threadId;
        }

        public String getThreadName() {
            return threadName;
        }

        public String getPurpose() {
            return purpose;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public long getAgeMs() {
            return System.currentTimeMillis() - createdAt;
        }
    }

    public static void register(String purpose) {
        Thread current = Thread.currentThread();
        ThreadMetadata metadata = new ThreadMetadata(current.threadId(), current.getName(), purpose);
        THREAD_METADATA.put(current.threadId(), metadata);
        GameLogger.setThreadContext("purpose", purpose);
        GameLogger.logThreadInfo("Thread registered: " + purpose);
    }

    public static void unregister() {
        Thread current = Thread.currentThread();
        ThreadMetadata metadata = THREAD_METADATA.remove(current.threadId());
        if (metadata != null) {
            GameLogger.logThreadInfo(
                    "Thread unregistered: " + metadata.getPurpose() + " (lived " + metadata.getAgeMs() + "ms)");
        }
        GameLogger.clearThreadContext();
        CONTEXT.remove();
    }

    /**
     * Sets a context value for the current thread.
     */
    public static void set(String key, Object value) {
        CONTEXT.get().put(key, value);
        GameLogger.setThreadContext(key, value);
    }

    /**
     * Gets a context value for the current thread.
     */
    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    /**
     * Removes a context value for the current thread.
     */
    public static void remove(String key) {
        CONTEXT.get().remove(key);
    }

    /**
     * Gets all context values for the current thread.
     */
    public static Map<String, Object> getAll() {
        return new HashMap<>(CONTEXT.get());
    }

    /**
     * Clears all context values for the current thread.
     */
    public static void clear() {
        CONTEXT.get().clear();
        GameLogger.clearThreadContext();
    }

    /**
     * Gets metadata for the current thread.
     */
    public static ThreadMetadata getCurrentMetadata() {
        return THREAD_METADATA.get(Thread.currentThread().threadId());
    }

    /**
     * Gets metadata for a specific thread ID.
     */
    public static ThreadMetadata getMetadata(long threadId) {
        return THREAD_METADATA.get(threadId);
    }

    /**
     * Gets all registered thread metadata.
     */
    public static Map<Long, ThreadMetadata> getAllMetadata() {
        return new HashMap<>(THREAD_METADATA);
    }
}
