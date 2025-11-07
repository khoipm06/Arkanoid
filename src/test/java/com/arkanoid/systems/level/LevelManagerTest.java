package com.arkanoid.systems.level;

import com.arkanoid.core.entities.Brick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LevelManagerTest {

    private LevelManager levelManager;

    @BeforeEach
    void setUp() {
        levelManager = new LevelManager();
    }

    @Test
    void testLoadLevel() {
        // This test assumes that the test resources are correctly loaded.
        // In a real project, you might need to configure the build system to handle resources.
        List<Brick> bricks = levelManager.loadLevelFromFile("/levels/test_level.json");
        assertNotNull(bricks);
        assertEquals(2, bricks.size());
    }

    @Test
    void testLoadNonExistentLevel() {
        List<Brick> bricks = levelManager.loadLevelFromFile("/levels/non_existent_level.json");
        assertNotNull(bricks);
        assertTrue(bricks.isEmpty());
    }
}
