package com.arkanoid.debug;

import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.logging.GameLogger;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    private GameManager gameManager;
    private boolean enabled = false;
    
    // Track collection sizes over time
    private final Map<String, CollectionStats> collectionStats = new HashMap<>();
    
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
     * Enable or disable memory monitoring.
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
            logger.info("Memory monitoring ENABLED");
            scheduler.scheduleAtFixedRate(
                this::checkAndReport,
                0,
                REPORT_INTERVAL_SECONDS,
                TimeUnit.SECONDS
            );
        } else {
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
     * Check memory usage and report if over threshold or at intervals.
     */
    private void checkAndReport() {
        if (!enabled) {
            return;
        }
        
        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            long usedMB = heapUsage.getUsed() / (1024 * 1024);
            long maxMB = heapUsage.getMax() / (1024 * 1024);
            long committedMB = heapUsage.getCommitted() / (1024 * 1024);
            double usagePercent = (usedMB * 100.0) / maxMB;
            
            reportMemoryUsage(usedMB, maxMB, committedMB, usagePercent);
            
            // Alert if over threshold
            if (usedMB > ALERT_THRESHOLD_MB) {
                logger.error("[MEMORY ALERT] Usage over 3GB! Current: {} MB", usedMB);
                reportTopCollections();
                suggestGC();
            }
            
            // Track collections if GameManager is available
            if (gameManager != null) {
                trackCollections();
            }
            
        } catch (Exception e) {
            logger.error("Error in memory monitoring: {}", e.getMessage(), e);
        }
    }
    
    private void reportMemoryUsage(long usedMB, long maxMB, long committedMB, double usagePercent) {
        logger.info("[MEMORY] Used: {}/{} MB ({} MB committed) - {}%",
            usedMB, maxMB, committedMB, String.format("%.1f", usagePercent));
    }
    
    private void trackCollections() {
        try {
            updateStats("particles", gameManager.getParticles().size());
            updateStats("trailEffects", gameManager.getTrailEffects().size());
            updateStats("bullets", gameManager.getBullets().size());
            updateStats("floatingTexts", gameManager.getFloatingTexts().size());
            updateStats("powerUps", gameManager.getPowerUps().size());
            updateStats("explosions", gameManager.getExplosions().size());
            updateStats("lineEffects", gameManager.getLineEffects().size());
            updateStats("bricks", gameManager.getBricks().size());
            updateStats("balls", gameManager.getBalls().size());
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
        logger.info("[MEMORY] Suggesting garbage collection...");
        
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
     * Shutdown the memory monitor.
     */
    public void shutdown() {
        setMonitoringEnabled(false);
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
    
    /**
     * Statistics tracker for collection sizes.
     */
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
