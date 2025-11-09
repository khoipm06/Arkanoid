package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.Paddle;
import com.arkanoid.core.entities.PowerUp;
import com.arkanoid.systems.player.Orientation;
import com.arkanoid.systems.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of PowerUpService for two-player mode. Manages power-up
 * spawning, movement, and ownership.
 */
public class PowerUpServiceImpl implements PowerUpService {

    private final Player player1;
    private final Player player2;
    private final List<PowerUpInstance> activePowerUps;

    /**
     * Internal class to track power-up ownership and movement.
     */
    private static class PowerUpInstance {
        PowerUp powerUp;
        int ownerPlayerNumber;

        PowerUpInstance(PowerUp powerUp, int ownerPlayerNumber) {
            this.powerUp = powerUp;
            this.ownerPlayerNumber = ownerPlayerNumber;
        }
    }

    public PowerUpServiceImpl(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.activePowerUps = new ArrayList<>();
    }

    @Override
    public void spawn(double brickX, double brickY, int ownerPlayerNumber) {
        Player owner = (ownerPlayerNumber == 1) ? player1 : player2;
        Orientation orientation = owner.getOrientation();

        // Set directional velocity toward owner's paddle
        // BOTTOM player: downward (positive Y), TOP player: upward (negative Y)
        double velocityY = (orientation == Orientation.BOTTOM) ? 100 : -100;

        // Create random power-up
        PowerUp powerUp = createRandomPowerUp(brickX, brickY);
        if (powerUp != null) {
            powerUp.setVelocityY(velocityY);
            activePowerUps.add(new PowerUpInstance(powerUp, ownerPlayerNumber));
            System.out.println("Power-up spawned at (" + brickX + ", " + brickY + ") for Player " + ownerPlayerNumber
                    + ": " + powerUp.getClass().getSimpleName());
        }
    }

    /**
     * Creates a random power-up at the specified location.
     */
    private PowerUp createRandomPowerUp(double x, double y) {
        double random = Math.random();

        if (random < 0.2) {
            return new com.arkanoid.core.entities.ExpandPaddlePowerUp(x, y);
        } else if (random < 0.4) {
            return new com.arkanoid.core.entities.MultiBallPowerUp(x, y);
        } else if (random < 0.6) {
            return new com.arkanoid.core.entities.GunPaddlePowerUp(x, y);
        } else if (random < 0.8) {
            return new com.arkanoid.core.entities.ExplosiveBallPowerUp(x, y);
        } else {
            return new com.arkanoid.core.entities.RowClearPowerUp(x, y);
        }
    }

    @Override
    public void updateAll(double deltaTime) {
        Iterator<PowerUpInstance> iterator = activePowerUps.iterator();

        while (iterator.hasNext()) {
            PowerUpInstance instance = iterator.next();
            PowerUp powerUp = instance.powerUp;

            // Update power-up position
            powerUp.update(deltaTime);

            // Check if collected by owner
            Player owner = (instance.ownerPlayerNumber == 1) ? player1 : player2;
            Paddle ownerPaddle = owner.getPaddle();

            if (powerUp.intersects(ownerPaddle) && !powerUp.isCollected()) {
                applyPickup(instance.ownerPlayerNumber, powerUp);
                powerUp.setCollected(true);
                iterator.remove();
            }

            // Remove if expired or out of bounds
            if (!powerUp.isActive() || isOutOfBounds(powerUp)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void applyPickup(int playerNumber, PowerUp powerUp) {
        Player player = (playerNumber == 1) ? player1 : player2;
        Paddle paddle = player.getPaddle();

        // Apply effect only to owner player
        // Filter out opponent-affecting power-ups for fairness
        powerUp.applyEffect(paddle);

        System.out.println("Player " + playerNumber + " collected power-up: " + powerUp.getClass().getSimpleName());
    }

    @Override
    public void clear() {
        activePowerUps.clear();
        System.out.println("All power-ups cleared.");
    }

    /**
     * Checks if power-up has moved out of playable bounds.
     */
    private boolean isOutOfBounds(PowerUp powerUp) {
        // Simple bounds check - assumes standard playfield
        return powerUp.getY() < -50 || powerUp.getY() > 650;
    }
}
