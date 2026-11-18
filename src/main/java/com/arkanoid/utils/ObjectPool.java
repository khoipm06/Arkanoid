package com.arkanoid.utils;

import java.util.Collection;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Thread-safe bounded object pool to reduce garbage collection pressure.
 *
 * @param <T> The type of objects to pool
 */
public class ObjectPool<T> {
    private final Queue<T> available;
    private final Supplier<T> factory;
    private final Consumer<T> resetAction;
    private final int maxSize;
    private final AtomicInteger availableCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);

    /**
     * Creates a new bounded object pool.
     *
     * @param factory     Function to create new objects when the pool is empty
     * @param resetAction Function to reset object state on release (may be null)
     * @param maxSize     Maximum number of available objects to retain in the pool
     */
    public ObjectPool(Supplier<T> factory, Consumer<T> resetAction, int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize must be non-negative: " + maxSize);
        }
        this.available = new ConcurrentLinkedQueue<>();
        this.factory = Objects.requireNonNull(factory, "factory must not be null");
        this.resetAction = resetAction;
        this.maxSize = maxSize;
    }

    /**
     * Acquires an object from the pool, or creates a new one if empty.
     *
     * @return An object ready for use
     */
    public T acquire() {
        T obj = available.poll();
        if (obj != null) {
            availableCount.decrementAndGet();
            return obj;
        }
        totalCount.incrementAndGet();
        return factory.get();
    }

    /**
     * Returns an object to the pool, running {@code resetAction} before making it available.
     * Discards the object if the pool has reached {@code maxSize}.
     *
     * @param obj The object to return (null is ignored)
     */
    public void release(T obj) {
        if (obj == null) {
            return;
        }

        while (true) {
            int current = availableCount.get();
            if (current >= maxSize) {
                totalCount.updateAndGet(n -> Math.max(0, n - 1));
                return;
            }
            if (availableCount.compareAndSet(current, current + 1)) {
                try {
                    if (resetAction != null) {
                        resetAction.accept(obj);
                    }
                    available.offer(obj);
                } catch (RuntimeException e) {
                    availableCount.decrementAndGet();
                    throw e;
                }
                return;
            }
        }
    }

    /**
     * Releases all non-null objects in the given collection back to the pool.
     *
     * @param items Collection of objects to release (null is ignored)
     */
    public void releaseAll(Collection<T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (T item : items) {
            release(item);
        }
    }

    /**
     * Prewarms the pool by creating up to {@code count} objects in advance.
     *
     * @param count Number of objects to create
     */
    public void prewarm(int count) {
        for (int i = 0; i < count; i++) {
            int current = availableCount.get();
            if (current >= maxSize) {
                break;
            }
            if (availableCount.compareAndSet(current, current + 1)) {
                T obj = factory.get();
                try {
                    if (resetAction != null) {
                        resetAction.accept(obj);
                    }
                    available.offer(obj);
                    totalCount.incrementAndGet();
                } catch (RuntimeException e) {
                    availableCount.decrementAndGet();
                    throw e;
                }
            }
        }
    }

    /**
     * Gets the number of available objects currently in the pool.
     */
    public int getAvailableCount() {
        return availableCount.get();
    }

    /**
     * Gets the total number of live/pooled objects tracked by this pool.
     */
    public int getTotalCount() {
        return totalCount.get();
    }

    /**
     * Gets the maximum pool capacity.
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Clears all pooled objects.
     */
    public void clear() {
        available.clear();
        availableCount.set(0);
        totalCount.set(0);
    }
}
