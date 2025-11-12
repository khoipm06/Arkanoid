package com.arkanoid.systems.threading;

import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized manager for thread pools and concurrent execution. Provides named
 * thread pools with graceful shutdown.
 */
public class ThreadManager {
    private static final Logger logger = GameLogger.getLogger(ThreadManager.class);
    private static ThreadManager instance;

    private final ExecutorService gameLoopExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final ExecutorService backgroundExecutor;

    private final AtomicInteger gameThreadCounter = new AtomicInteger(0);
    private final AtomicInteger scheduledThreadCounter = new AtomicInteger(0);
    private final AtomicInteger backgroundThreadCounter = new AtomicInteger(0);

    private ThreadManager() {
        // Single thread for game loop (JavaFX already manages this, but keeping for
        // extensibility)
        this.gameLoopExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GameLoop-" + gameThreadCounter.incrementAndGet());
            t.setDaemon(false);
            return t;
        });

        // Scheduled executor for timed tasks
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Scheduled-" + scheduledThreadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });

        // Background thread pool for async operations
        this.backgroundExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "Background-" + backgroundThreadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });

        logger.info("ThreadManager initialized with 3 thread pools");
    }

    /**
     * Gets the singleton instance.
     */
    public static synchronized ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    /**
     * Executes a task on the game loop thread. Use for game state modifications
     * that must be single-threaded.
     */
    public Future<?> executeOnGameLoop(Runnable task, String purpose) {
        logger.debug("Submitting task to GameLoop: {}", purpose);
        return gameLoopExecutor.submit(() -> {
            ThreadContext.register(purpose);
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in GameLoop task: {}", purpose, e);
                throw e;
            } finally {
                ThreadContext.unregister();
            }
        });
    }

    /**
     * Schedules a recurring task with fixed delay.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit,
            String purpose) {
        logger.debug("Scheduling recurring task: {} (initial: {}ms, delay: {}ms)", purpose, unit.toMillis(initialDelay),
                unit.toMillis(delay));
        return scheduledExecutor.scheduleWithFixedDelay(() -> {
            ThreadContext.register(purpose);
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in scheduled task: {}", purpose, e);
            } finally {
                ThreadContext.unregister();
            }
        }, initialDelay, delay, unit);
    }

    /**
     * Schedules a one-time delayed task.
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit, String purpose) {
        logger.debug("Scheduling one-time task: {} (delay: {}ms)", purpose, unit.toMillis(delay));
        return scheduledExecutor.schedule(() -> {
            ThreadContext.register(purpose);
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in scheduled task: {}", purpose, e);
            } finally {
                ThreadContext.unregister();
            }
        }, delay, unit);
    }

    /**
     * Executes a task on a background thread. Use for I/O operations, database
     * queries, or heavy computations.
     */
    public Future<?> executeBackground(Runnable task, String purpose) {
        logger.debug("Submitting background task: {}", purpose);
        return backgroundExecutor.submit(() -> {
            ThreadContext.register(purpose);
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in background task: {}", purpose, e);
                throw e;
            } finally {
                ThreadContext.unregister();
            }
        });
    }

    /**
     * Executes a callable task on a background thread with result.
     */
    public <T> Future<T> executeBackground(Callable<T> task, String purpose) {
        logger.debug("Submitting background callable: {}", purpose);
        return backgroundExecutor.submit(() -> {
            ThreadContext.register(purpose);
            try {
                return task.call();
            } catch (Exception e) {
                logger.error("Error in background callable: {}", purpose, e);
                throw e;
            } finally {
                ThreadContext.unregister();
            }
        });
    }

    /**
     * Gracefully shuts down all thread pools. Waits for tasks to complete within
     * timeout.
     */
    public void shutdown() {
        logger.info("ThreadManager shutting down...");

        gameLoopExecutor.shutdown();
        scheduledExecutor.shutdown();
        backgroundExecutor.shutdown();

        try {
            if (!gameLoopExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("GameLoop executor did not terminate in time, forcing shutdown");
                gameLoopExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Scheduled executor did not terminate in time, forcing shutdown");
                scheduledExecutor.shutdownNow();
            }
            if (!backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Background executor did not terminate in time, forcing shutdown");
                backgroundExecutor.shutdownNow();
            }
            logger.info("ThreadManager shutdown complete");
        } catch (InterruptedException e) {
            logger.error("ThreadManager shutdown interrupted", e);
            Thread.currentThread().interrupt();
            gameLoopExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
            backgroundExecutor.shutdownNow();
        }
    }

    /**
     * Checks if all executors are terminated.
     */
    public boolean isTerminated() {
        return gameLoopExecutor.isTerminated() && scheduledExecutor.isTerminated() && backgroundExecutor.isTerminated();
    }
}
