package com.arkanoid.systems.threading;

import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized manager for thread pools and concurrent execution. Provides named
 * daemon thread pools with idempotent graceful shutdown.
 */
public class ThreadManager {
    private static final Logger logger = GameLogger.getLogger(ThreadManager.class);
    private static volatile ThreadManager instance;

    private final ExecutorService gameLoopExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final ExecutorService backgroundExecutor;

    private final AtomicInteger gameThreadCounter = new AtomicInteger(0);
    private final AtomicInteger scheduledThreadCounter = new AtomicInteger(0);
    private final AtomicInteger backgroundThreadCounter = new AtomicInteger(0);
    private final AtomicBoolean shutdownCalled = new AtomicBoolean(false);

    private ThreadManager() {
        this.gameLoopExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GameLoop-" + gameThreadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });

        this.scheduledExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Scheduled-" + scheduledThreadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });

        this.backgroundExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "Background-" + backgroundThreadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });

        logger.info("ThreadManager initialized with 3 daemon thread pools");
    }

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

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
     * Gracefully shuts down all thread pools. Idempotent and safe to call multiple times.
     */
    public synchronized void shutdown() {
        if (!shutdownCalled.compareAndSet(false, true)) {
            return;
        }
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

    public boolean isTerminated() {
        return gameLoopExecutor.isTerminated() && scheduledExecutor.isTerminated() && backgroundExecutor.isTerminated();
    }
}
