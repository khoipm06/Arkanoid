package com.arkanoid.utils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Generic object pool to reduce garbage collection pressure.
 * Reuses objects instead of creating/destroying them constantly.
 * Thread-safe implementation using ConcurrentLinkedQueue.
 * 
 * @param <T> The type of objects to pool
 */
public class ObjectPool<T> {
    private final Queue<T> available;
    private final Supplier<T> factory;
    private final Consumer<T> reset;
    private final int maxSize;
    private int currentSize;

    /**
     * Creates a new object pool.
     * 
     * @param factory Function to create new objects when pool is empty
     * @param reset Function to reset object state before reuse
     * @param maxSize Maximum number of objects to keep in pool
     */
    public ObjectPool(Supplier<T> factory, Consumer<T> reset, int maxSize) {
        this.available = new ConcurrentLinkedQueue<>();
        this.factory = factory;
        this.reset = reset;
        this.maxSize = maxSize;
        this.currentSize = 0;
    }

    /**
     * Acquires an object from the pool.
     * Creates a new one if pool is empty.
     * 
     * @return An object ready for use
     */
    public T acquire() {
        T obj = available.poll();
        if (obj == null) {
            obj = factory.get();
            currentSize++;
        }
        return obj;
    }

    /**
     * Returns an object to the pool for reuse.
     * Resets the object state before returning to pool.
     * Discards if pool is at max capacity.
     * 
     * @param obj The object to return
     */
    public void release(T obj) {
        if (obj == null) return;
        
        if (available.size() < maxSize) {
            reset.accept(obj);
            available.offer(obj);
        } else {
            // Pool is full, let GC handle it
            currentSize--;
        }
    }

    /**
     * Prewarms the pool by creating objects in advance.
     * 
     * @param count Number of objects to create
     */
    public void prewarm(int count) {
        for (int i = 0; i < count && currentSize < maxSize; i++) {
            available.offer(factory.get());
            currentSize++;
        }
    }

    /**
     * Gets the number of available objects in the pool.
     * 
     * @return Available object count
     */
    public int getAvailableCount() {
        return available.size();
    }

    /**
     * Gets the total number of objects created by this pool.
     * 
     * @return Total object count
     */
    public int getTotalCount() {
        return currentSize;
    }

    /**
     * Clears all objects from the pool.
     */
    public void clear() {
        available.clear();
        currentSize = 0;
    }
}
