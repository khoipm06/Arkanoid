package com.arkanoid.ui.view;

import com.arkanoid.core.GameConstants;
import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Brick;
import com.arkanoid.core.entities.Paddle;
import com.arkanoid.core.physics.CollisionDetector;
import com.arkanoid.systems.input.TwoPlayerInputHandler;
import com.arkanoid.systems.level.LevelManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.Player;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.systems.twoplayer.*;
import com.arkanoid.utils.ImageLoader;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main game screen for two-player mode. Manages game rendering, input, and
 * match coordination inside {@link SceneManager}'s unified viewport.
 */
public class TwoPlayerGameScreen implements SceneLifecycle {
    private static final Logger logger = GameLogger.getLogger(TwoPlayerGameScreen.class);

    private static final String MENU_BUTTON_NORMAL_STYLE =
            "-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298);"
                    + "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: white;"
                    + "-fx-border-width: 2; -fx-cursor: hand;";

    private static final String MENU_BUTTON_HOVER_STYLE =
            "-fx-background-color: linear-gradient(to bottom right, #2a5298, #1e3c72);"
                    + "-fx-text-fill: yellow; -fx-font-size: 22px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: white;"
                    + "-fx-border-width: 2; -fx-cursor: hand;";

    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final Image backgroundImage;

    private Player player1;
    private Player player2;
    private TwoPlayerMatchManager matchManager;
    private TwoPlayerInputHandler inputHandler;
    private TwoPlayerStatsPanel statsPanel;
    private List<Brick> bricks;
    private LevelManager levelManager;

    private AnimationTimer gameLoop;
    private long lastFrameTime = 0L;
    private boolean gameOverTriggered = false;
    private VBox pauseMenu;
    private StackPane pauseOverlay;
    private StackPane rawRoot;
    private StackPane scaledRoot;
    private BorderPane gameLayout;

    public TwoPlayerGameScreen(Stage stage) {
        this.stage = stage != null ? stage : SceneManager.getStage();
        this.canvas = new Canvas(GameConstants.TWO_PLAYER_CANVAS_WIDTH, GameConstants.TWO_PLAYER_CANVAS_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.backgroundImage = ImageLoader.loadImage("/images/backgroundGame.png");
    }

    @Override
    public void onExit() {
        cleanup();
    }

    public void show() {
        gameLayout = new BorderPane();
        gameLayout.setCenter(canvas);
        gameLayout.setStyle("-fx-background-color: black;");

        initializeGame();

        statsPanel = new TwoPlayerStatsPanel(player1, player2);
        gameLayout.setRight(statsPanel);

        rawRoot = new StackPane(gameLayout);
        scaledRoot = ViewportScaler.wrap(
                rawRoot,
                GameConstants.TWO_PLAYER_DESIGN_WIDTH,
                GameConstants.TWO_PLAYER_CANVAS_HEIGHT
        );

        SceneManager.showCustomRoot("twoPlayerGame", scaledRoot, this);
        SceneManager.setKeyHandlers(
                e -> {
                    activeKeys.add(e.getCode());
                    if (inputHandler != null) {
                        inputHandler.handleKeyPress(e.getCode());
                    }
                },
                e -> activeKeys.remove(e.getCode())
        );

        gameOverTriggered = false;
        startGameLoop();
    }

    private void initializeGame() {
        double canvasW = GameConstants.TWO_PLAYER_CANVAS_WIDTH;
        double canvasH = GameConstants.TWO_PLAYER_CANVAS_HEIGHT;
        double ballSpeed = GameConstants.DEFAULT_BALL_SPEED;
        double paddleSpeed = GameConstants.DEFAULT_PADDLE_SPEED;
        double ballRadius = GameConstants.DEFAULT_BALL_RADIUS;

        levelManager = new LevelManager();
        bricks = levelManager.loadLevel(0);
        logger.info("Loaded {} bricks for two-player mode", bricks.size());

        // Player 1 (bottom)
        Paddle paddle1 = new Paddle(canvasW / 2 - 50, canvasH - 40, 100, 15, paddleSpeed, 0, canvasW);
        player1 = new Player("player1", 1, paddle1);
        Ball ball1 = new Ball(canvasW / 2, canvasH - 60, ballRadius, ballSpeed);
        ball1.setBounds(0, 0, canvasW, canvasH);
        ball1.setTopIsDeadSide(false);
        player1.setBall(ball1);

        // Player 2 (top)
        Paddle paddle2 = new Paddle(canvasW / 2 - 50, 25, 100, 15, paddleSpeed, 0, canvasW);
        player2 = new Player("player2", 2, paddle2);
        Ball ball2 = new Ball(canvasW / 2, 45, ballRadius, ballSpeed);
        ball2.setBounds(0, 0, canvasW, canvasH);
        ball2.setTopIsDeadSide(true);
        player2.setBall(ball2);

        CollisionService collisionService = new CollisionServiceImpl(player1, player2);
        RespawnService respawnService = new RespawnServiceImpl(player1, player2, ballSpeed);
        PowerUpService powerUpService = new PowerUpServiceImpl(player1, player2);

        matchManager = new TwoPlayerMatchManagerImpl(
                player1, player2, collisionService, respawnService, powerUpService, bricks);

        inputHandler = new TwoPlayerInputHandler(player1, player2, matchManager);
        inputHandler.setOnPauseCallback(this::showPauseMenu);
        inputHandler.setOnResumeCallback(this::onResumed);

        matchManager.startMatch();
        logger.info("Two-player game initialized: {} bricks", bricks.size());
    }

    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        lastFrameTime = 0L;

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0L) {
                    lastFrameTime = now;
                    return;
                }
                double rawDelta = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;
                double deltaTime = Math.min(Math.max(rawDelta, 0.0), GameConstants.MAX_FRAME_DELTA);

                update(deltaTime);
                render();

                if (!gameOverTriggered && matchManager.getState() == TwoPlayerMatchManager.MatchState.GAME_OVER) {
                    gameOverTriggered = true;
                    gameLoop.stop();
                    showGameOver();
                }
            }
        };

        gameLoop.start();
    }

    private void update(double deltaTime) {
        if (matchManager.getState() != TwoPlayerMatchManager.MatchState.PLAYING) {
            return;
        }

        inputHandler.handleContinuousInput(activeKeys, deltaTime);
        checkBrickCollisions();

        for (Brick brick : bricks) {
            brick.update(deltaTime);
        }

        matchManager.update(deltaTime);
    }

    private void checkBrickCollisions() {
        Ball ball1 = player1.getBall();
        Ball ball2 = player2.getBall();

        for (Brick brick : bricks) {
            if (brick.isDestroyed()) {
                continue;
            }

            boolean hitThisFrame = false;

            if (ball1 != null && !ball1.isAttachedToPaddle() && brick.intersects(ball1)) {
                SoundManager.getInstance().playSound("brickBounce.wav");
                CollisionDetector.handleBallBrickCollision(ball1, brick);
                brick.hit();
                hitThisFrame = true;
                if (brick.isDestroyed()) {
                    logger.debug("Player 1 destroyed brick at ({}, {})", brick.getX(), brick.getY());
                    matchManager.applyBrickHit(1, 100);
                }
            }

            if (!hitThisFrame && ball2 != null && !ball2.isAttachedToPaddle() && brick.intersects(ball2)) {
                SoundManager.getInstance().playSound("brickBounce.wav");
                CollisionDetector.handleBallBrickCollision(ball2, brick);
                brick.hit();
                if (brick.isDestroyed()) {
                    logger.debug("Player 2 destroyed brick at ({}, {})", brick.getX(), brick.getY());
                    matchManager.applyBrickHit(2, 100);
                }
            }
        }

        bricks.removeIf(Brick::isDestroyed);
    }

    private void render() {
        double canvasW = GameConstants.TWO_PLAYER_CANVAS_WIDTH;
        double canvasH = GameConstants.TWO_PLAYER_CANVAS_HEIGHT;

        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, canvasW, canvasH);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvasW, canvasH);
        }

        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                brick.render(gc);
            }
        }

        player1.getPaddle().render(gc);
        player2.getPaddle().render(gc);

        if (player1.getBall() != null) {
            player1.getBall().render(gc);
        }
        if (player2.getBall() != null) {
            player2.getBall().render(gc);
        }

        if (statsPanel != null) {
            statsPanel.update();
        }
    }

    private void onResumed() {
        lastFrameTime = 0L;
        activeKeys.clear();
        hidePauseMenu();
    }

    private void resumeGame() {
        matchManager.resume();
        onResumed();
    }

    private void showPauseMenu() {
        if (pauseOverlay != null) {
            return;
        }

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(10, 20, 35, 0.95), rgba(5, 10, 20, 0.95));"
                + "-fx-padding: 40;"
                + "-fx-border-radius: 20;"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: linear-gradient(to right, #00d4ff, #0080ff, #00d4ff);"
                + "-fx-border-width: 3;"
                + "-fx-effect: dropshadow(gaussian, rgba(0, 212, 255, 0.6), 30, 0, 0, 0);");
        pauseMenu.setMaxSize(450, 400);
        pauseMenu.setMinSize(450, 400);
        pauseMenu.setPrefSize(450, 400);

        Text title = new Text("GAME PAUSED");
        title.setFont(Font.font("Arial Black", 42));
        title.setFill(Color.WHITE);
        title.setStroke(Color.CYAN);
        title.setStrokeWidth(2);

        Button resumeBtn = createMenuButton("Resume");
        resumeBtn.setOnAction(e -> resumeGame());

        Button restartBtn = createMenuButton("Restart");
        restartBtn.setOnAction(e -> restartGame());

        Button backBtn = createMenuButton("Back to Menu");
        backBtn.setOnAction(e -> backToMenu());

        pauseMenu.getChildren().addAll(title, resumeBtn, restartBtn, backBtn);
        overlay.getChildren().add(pauseMenu);

        pauseOverlay = overlay;
        rawRoot.getChildren().add(pauseOverlay);
    }

    private void hidePauseMenu() {
        if (pauseOverlay != null && rawRoot != null) {
            rawRoot.getChildren().remove(pauseOverlay);
            pauseOverlay = null;
            pauseMenu = null;
        }
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setStyle(MENU_BUTTON_NORMAL_STYLE);
        button.setPrefWidth(250);
        button.setPrefHeight(50);

        button.setOnMouseEntered(e -> button.setStyle(MENU_BUTTON_HOVER_STYLE));
        button.setOnMouseExited(e -> button.setStyle(MENU_BUTTON_NORMAL_STYLE));

        return button;
    }

    private void restartGame() {
        cleanup();
        initializeGame();
        if (statsPanel != null) {
            statsPanel.bindPlayers(player1, player2);
        }
        gameOverTriggered = false;
        startGameLoop();
    }

    private void backToMenu() {
        SceneManager.switchTo("mainMenuView");
    }

    /**
     * Stops the game loop, clears active keys, and removes any open pause overlay.
     */
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
            logger.info("Two-player game loop stopped");
        }
        activeKeys.clear();
        hidePauseMenu();
    }

    private void showGameOver() {
        TwoPlayerMatchManagerImpl managerImpl = (TwoPlayerMatchManagerImpl) matchManager;

        logger.info("Two-player game ended: Winner={}, Reason={}, P1Score={}, P2Score={}",
                managerImpl.getWinningPlayer(), managerImpl.getEndReason(),
                player1.getState().getScore(), player2.getState().getScore());

        TwoPlayerGameOverScreen gameOverScreen = new TwoPlayerGameOverScreen(
                stage,
                managerImpl.getWinningPlayer(),
                managerImpl.getEndReason(),
                player1.getState().getScore(),
                player2.getState().getScore()
        );
        gameOverScreen.show();
    }
}
