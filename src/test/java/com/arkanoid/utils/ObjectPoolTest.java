package com.arkanoid.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObjectPoolTest {

    private static final class DummyItem {
        boolean active = true;
        int resetCount = 0;
    }

    @Test
    void testAcquireAndReleaseRunsResetAndReusesInstance() {
        AtomicInteger created = new AtomicInteger(0);
        ObjectPool<DummyItem> pool = new ObjectPool<>(
                () -> {
                    created.incrementAndGet();
                    return new DummyItem();
                },
                item -> {
                    item.active = false;
                    item.resetCount++;
                },
                5
        );

        DummyItem first = pool.acquire();
        assertEquals(1, created.get());
        assertTrue(first.active);

        pool.release(first);
        assertFalse(first.active);
        assertEquals(1, first.resetCount);
        assertEquals(1, pool.getAvailableCount());

        DummyItem reacquired = pool.acquire();
        assertSame(first, reacquired);
        assertEquals(1, created.get());
        assertEquals(0, pool.getAvailableCount());
    }

    @Test
    void testReleaseRespectsMaxSize() {
        ObjectPool<DummyItem> pool = new ObjectPool<>(
                DummyItem::new,
                item -> item.resetCount++,
                2
        );

        DummyItem a = pool.acquire();
        DummyItem b = pool.acquire();
        DummyItem c = pool.acquire();

        pool.release(a);
        pool.release(b);
        pool.release(c);

        assertEquals(2, pool.getAvailableCount());
        assertEquals(1, a.resetCount);
        assertEquals(1, b.resetCount);
        assertEquals(0, c.resetCount);
    }

    @Test
    void testReleaseAllRunsResetAndCapsAtMaxSize() {
        ObjectPool<DummyItem> pool = new ObjectPool<>(
                DummyItem::new,
                item -> {
                    item.active = false;
                    item.resetCount++;
                },
                2
        );

        List<DummyItem> items = List.of(new DummyItem(), new DummyItem(), new DummyItem());
        pool.releaseAll(items);

        assertEquals(2, pool.getAvailableCount());
        assertFalse(items.get(0).active);
        assertFalse(items.get(1).active);
        assertEquals(1, items.get(0).resetCount);
        assertEquals(1, items.get(1).resetCount);
    }

    @Test
    void testPrewarmAndClear() {
        ObjectPool<DummyItem> pool = new ObjectPool<>(DummyItem::new, item -> item.active = false, 3);
        pool.prewarm(5);
        assertEquals(3, pool.getAvailableCount());
        assertEquals(3, pool.getTotalCount());

        pool.clear();
        assertEquals(0, pool.getAvailableCount());
        assertEquals(0, pool.getTotalCount());
    }
}
