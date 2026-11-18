package com.arkanoid.debug;

import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Memory monitoring utility for tracking heap usage and detecting potential leaks.
 * Reports memory statistics every 5 seconds when enabled.
 */
public class MemoryMonitor {
    private static final Logger logger = GameLogger.getLogger(MemoryMonitor.class);
    private static final MemoryMonitor instance = new MemoryMonitor();
    private static final long REPORT_INTERVAL_SECONDS = 5;
    private static final long ALERT_THRESHOLD_MB = 3072; // 3GB

    private final MemoryMXBean memoryBean;
    private final ScheduledExecutorService scheduler;
    private volatile GameManager gameManager;
    private boolean enabled = false;
    private ScheduledFuture<?> monitoringTask;

    private final Map<String, CollectionStats> collectionStats = new ConcurrentHashMap<>();

    private MemoryMonitor() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MemoryMonitor");
            t.setDaemon(true);
            return t;
        });
    }

    public static MemoryMonitor getInstance() {
        return instance;
    }

    /**
     * Enable or disable memory monitoring without scheduling duplicate tasks.
     */
    public static void setEnabled(boolean enabled) {
        getInstance().setMonitoringEnabled(enabled);
    }

    private synchronized void setMonitoringEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            if (scheduler.isShutdown()) {
                this.enabled = false;
                return;
            }
            if (monitoringTask == null || monitoringTask.isCancelled() || monitoringTask.isDone()) {
                monitoringTask = scheduler.scheduleAtFixedRate(
                        this::checkAndReport,
                        0,
                        REPORT_INTERVAL_SECONDS,
                        TimeUnit.SECONDS
                );
            }
            logger.info("Memory monitoring ENABLED");
        } else {
            if (monitoringTask != null) {
                monitoringTask.cancel(false);
                monitoringTask = null;
            }
            logger.info("Memory monitoring DISABLED");
        }
    }

    /**
     * Track a GameManager instance for collection monitoring.
     */
    public void trackGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /**
     * Release the tracked GameManager reference so finished sessions can be garbage collected.
     */
    public void untrackGameManager() {
        this.gameManager = null;
    }

    private void checkAndReport() {
        if (!enabled) {
            return;
        }

        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            long usedMB = heapUsage.getUsed() / (1024 * 1024);
            long maxMB = heapUsage.getMax() / (1024 * 1024);
            long committedMB = heapUsage.getCommitted() / (1024 * 1024);
            double usagePercent = maxMB > 0 ? (usedMB * 100.0) / maxMB : 0.0;

            reportMemoryUsage(usedMB, maxMB, committedMB, usagePercent);

            if (usedMB > ALERT_THRESHOLD_MB) {
                logger.error("[MEMORY ALERT] Usage over 3GB! Current: {} MB", usedMB);
                reportTopCollections();
                suggestGC();
            }

            GameManager gm = this.gameManager;
            if (gm != null) {
                trackCollections(gm);
            }
        } catch (Exception e) {
            logger.error("Error in memory monitoring: {}", e.getMessage(), e);
        }
    }

    private void reportMemoryUsage(long usedMB, long maxMB, long committedMB, double usagePercent) {
        logger.info("[MEMORY] Used: {}/{} MB ({} MB committed) - {}%",
                usedMB, maxMB, committedMB, String.format("%.1f", usagePercent));
    }

    private void trackCollections(GameManager gm) {
        try {
            updateStats("particles", gm.getParticles().size());
            updateStats("trailEffects", gm.getTrailEffects().size());
            updateStats("bullets", gm.getBullets().size());
            updateStats("floatingTexts", gm.getFloatingTexts().size());
            updateStats("powerUps", gm.getPowerUps().size());
            updateStats("explosions", gm.getExplosions().size());
            updateStats("lineEffects", gm.getLineEffects().size());
            updateStats("bricks", gm.getBricks().size());
            updateStats("balls", gm.getBalls().size());
        } catch (Exception e) {
            logger.debug("Error tracking collections: {}", e.getMessage());
        }
    }

    private void reportTopCollections() {
        if (collectionStats.isEmpty()) {
            return;
        }

        logger.info("[COLLECTIONS] Top collection sizes:");
        collectionStats.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().max, e1.getValue().max))
                .forEach(entry -> {
                    CollectionStats stats = entry.getValue();
                    logger.info("  {}: current={}, max={}, avg={}",
                            entry.getKey(), stats.current, stats.max, stats.getAverage());
                });
    }

    private void updateStats(String name, int size) {
        collectionStats.computeIfAbsent(name, k -> new CollectionStats())
                .update(size);
    }

    private void suggestGC() {
        long beforeMB = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        logger.info("[MEMORY] Suggesting garbage collection..., before: {} MB", beforeMB);

        System.gc();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long afterMB = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        logger.info("[MEMORY] After GC: {} MB", afterMB);
    }

    /**
     * Shutdown the memory monitor and release any tracked GameManager reference. Idempotent.
     */
    public synchronized void shutdown() {
        setMonitoringEnabled(false);
        untrackGameManager();
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class CollectionStats {
        int current = 0;
        int max = 0;
        long total = 0;
        int samples = 0;

        void update(int size) {
            this.current = size;
            this.max = Math.max(max, size);
            this.total += size;
            this.samples++;
        }

        int getAverage() {
            return samples > 0 ? (int) (total / samples) : 0;
        }
    }
}
