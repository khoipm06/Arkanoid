package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PowerUpServiceImplTest {

    @Test
    void testCollectPowerUpAtOutOfBoundsBoundaryDoesNotThrow() {
        Paddle paddle1 = new Paddle(100, 655, 120, 20, 400, 0, 700);
        Player player1 = new Player("p1", 1, paddle1);

        Paddle paddle2 = new Paddle(100, -70, 120, 20, 400, 0, 700);
        Player player2 = new Player("p2", 2, paddle2);

        PowerUpServiceImpl service = new PowerUpServiceImpl(player1, player2);
        service.spawn(120, 648, 1);
        service.spawn(120, -48, 2);

        assertDoesNotThrow(() -> service.updateAll(0.05));
    }

    @Test
    void testUncollectedOutOfBoundsPowerUpIsRemovedCleanly() {
        Paddle paddle1 = new Paddle(500, 560, 100, 20, 400, 0, 700);
        Player player1 = new Player("p1", 1, paddle1);

        Paddle paddle2 = new Paddle(500, 25, 100, 20, 400, 0, 700);
        Player player2 = new Player("p2", 2, paddle2);

        PowerUpServiceImpl service = new PowerUpServiceImpl(player1, player2);
        service.spawn(50, 648, 1);
        service.spawn(50, -48, 2);

        assertDoesNotThrow(() -> service.updateAll(0.05));
        assertDoesNotThrow(() -> service.updateAll(0.05));
    }
}
