package com.arkanoid.systems.twoplayer;

import com.arkanoid.core.entities.Brick;
import com.arkanoid.systems.player.Player;

import java.util.List;

/**
 * Implementation of TwoPlayerMatchManager.
 * Manages game state, scoring, life loss, and win conditions for two-player
 * competitive mode.
 */
public class TwoPlayerMatchManagerImpl implements TwoPlayerMatchManager {

    private final Player player1;
    private final Player player2;
    private final CollisionService collisionService;
    private final RespawnService respawnService;
    private final PowerUpService powerUpService;
    private final List<Brick> bricks;

    private MatchState state;
    private int winningPlayer; // 0 for draw, 1 or 2 for winner
    private EndReason endReason;

    // Life loss guard - prevents double life loss in same frame
    private boolean lifeLossProcessed = false;

    // Win condition constants
    private static final int WINNING_SCORE = 10000;

    public TwoPlayerMatchManagerImpl(Player player1, Player player2,
            CollisionService collisionService,
            RespawnService respawnService,
            PowerUpService powerUpService) {
        this(player1, player2, collisionService, respawnService, powerUpService, null);
    }

    public TwoPlayerMatchManagerImpl(Player player1, Player player2,
            CollisionService collisionService,
            RespawnService respawnService,
            PowerUpService powerUpService,
            List<Brick> bricks) {
        this.player1 = player1;
        this.player2 = player2;
        this.collisionService = collisionService;
        this.respawnService = respawnService;
        this.powerUpService = powerUpService;
        this.bricks = bricks;
        this.state = MatchState.READY;
        this.winningPlayer = 0;
    }

    @Override
    public void startMatch() {
        if (state == MatchState.READY) {
            state = MatchState.PLAYING;
            System.out.println("Two-player match started!");
        }
    }

    @Override
    public void update(double deltaTime) {
        if (state != MatchState.PLAYING) {
            return;
        }

        // Reset life loss guard each frame
        lifeLossProcessed = false;

        // Update both players
        player1.update(deltaTime);
        player2.update(deltaTime);

        // Check ball-ball collision
        if (player1.getBall() != null && player2.getBall() != null) {
            if (collisionService.checkBallBallCollision(player1.getBall(), player2.getBall())) {
                collisionService.resolveBallBallCollision(player1.getBall(), player2.getBall());
            }
        }

        // Check opponent paddle hits
        if (player1.getBall() != null && !player1.getBall().isAttachedToPaddle()) {
            if (collisionService.checkOpponentPaddleHit(player1.getBall(), 1)) {
                handleLifeLoss(2, LifeLossCause.OPPONENT_BALL);
            }
        }
        if (player2.getBall() != null && !player2.getBall().isAttachedToPaddle()) {
            if (collisionService.checkOpponentPaddleHit(player2.getBall(), 2)) {
                handleLifeLoss(1, LifeLossCause.OPPONENT_BALL);
            }
        }

        // Check Dead Side drains - each player loses life only if ball passes THEIR dead side
        if (player1.getBall() != null && player1.getBall().isOutOfBounds()) {
            handleLifeLoss(1, LifeLossCause.DEAD_SIDE);
        }
        if (player2.getBall() != null && player2.getBall().isOutOfBounds()) {
            handleLifeLoss(2, LifeLossCause.DEAD_SIDE);
        }

        // Update power-ups
        powerUpService.updateAll(deltaTime);

        // Check win conditions
        checkWinConditions();
    }

    @Override
    public void applyBrickHit(int playerNumber, int brickValue) {
        Player player = (playerNumber == 1) ? player1 : player2;
        player.getState().addScore(brickValue);
        System.out.println(
                "Player " + playerNumber + " scored " + brickValue + " points. Total: " + player.getState().getScore());
    }

    @Override
    public void handleLifeLoss(int playerNumber, LifeLossCause cause) {
        // Guard against multiple life loss in same frame
        if (lifeLossProcessed) {
            return;
        }
        lifeLossProcessed = true;

        Player player = (playerNumber == 1) ? player1 : player2;
        player.getState().loseLife();

        System.out.println("Player " + playerNumber + " lost a life (" + cause + "). Lives remaining: "
                + player.getState().getLives());

        // Check if player lost all lives
        if (player.getState().getLives() <= 0) {
            int winnerNumber = (playerNumber == 1) ? 2 : 1;
            winningPlayer = winnerNumber;
            endMatch(EndReason.LIVES_DEPLETED);
        } else {
            // Respawn ball immediately with auto-launch
            respawnService.respawnBall(playerNumber);
        }
    }

    @Override
    public void endMatch(EndReason reason) {
        state = MatchState.GAME_OVER;
        endReason = reason;

        String resultMessage;
        switch (reason) {
            case SCORE_REACHED:
                resultMessage = "Player " + winningPlayer + " wins by reaching " + WINNING_SCORE + " points!";
                break;
            case LIVES_DEPLETED:
                resultMessage = "Player " + winningPlayer + " wins! Opponent lost all lives.";
                break;
            case BRICKS_CLEARED:
                if (winningPlayer == 0) {
                    resultMessage = "Draw! All bricks cleared with equal scores.";
                } else {
                    resultMessage = "Player " + winningPlayer + " wins! All bricks cleared.";
                }
                break;
            case DRAW:
                resultMessage = "Draw!";
                break;
            default:
                resultMessage = "Match ended.";
        }

        System.out.println("=== GAME OVER ===");
        System.out.println(resultMessage);
        System.out.println("Player 1 Score: " + player1.getState().getScore());
        System.out.println("Player 2 Score: " + player2.getState().getScore());
    }

    /**
     * Checks all win conditions each frame.
     */
    private void checkWinConditions() {
        // Check score-based win (10,000 points)
        if (player1.getState().getScore() >= WINNING_SCORE) {
            winningPlayer = 1;
            endMatch(EndReason.SCORE_REACHED);
            return;
        }
        if (player2.getState().getScore() >= WINNING_SCORE) {
            winningPlayer = 2;
            endMatch(EndReason.SCORE_REACHED);
            return;
        }

        // Check if all bricks are cleared
        if (bricks != null && areAllBricksDestroyed()) {
            int score1 = player1.getState().getScore();
            int score2 = player2.getState().getScore();

            if (score1 > score2) {
                winningPlayer = 1;
            } else if (score2 > score1) {
                winningPlayer = 2;
            } else {
                winningPlayer = 0; // Draw
            }

            endMatch(EndReason.BRICKS_CLEARED);
        }
    }

    /**
     * Checks if all bricks have been destroyed.
     */
    private boolean areAllBricksDestroyed() {
        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MatchState getState() {
        return state;
    }

    @Override
    public void pause() {
        if (state == MatchState.PLAYING) {
            state = MatchState.PAUSED;
            System.out.println("Match paused.");
        }
    }

    @Override
    public void resume() {
        if (state == MatchState.PAUSED) {
            state = MatchState.PLAYING;
            System.out.println("Match resumed.");
        }
    }

    public int getWinningPlayer() {
        return winningPlayer;
    }

    public EndReason getEndReason() {
        return endReason;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}
