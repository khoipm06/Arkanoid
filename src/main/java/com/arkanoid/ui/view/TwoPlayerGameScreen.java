package com.arkanoid.ui.view;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Brick;
import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.input.TwoPlayerInputHandler;
import com.arkanoid.systems.level.LevelManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.Player;
import com.arkanoid.systems.twoplayer.*;
import javafx.animation.AnimationTimer;
import org.slf4j.Logger;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main game screen for two-player mode. Manages game rendering, input, and
 * match coordination.
 */
public class TwoPlayerGameScreen {
    private static final Logger logger = GameLogger.getLogger(TwoPlayerGameScreen.class);

    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Set<KeyCode> activeKeys = new HashSet<>();

    private Player player1;
    private Player player2;
    private TwoPlayerMatchManager matchManager;
    private TwoPlayerInputHandler inputHandler;
    private TwoPlayerStatsPanel statsPanel;
    private List<Brick> bricks;
    private LevelManager levelManager;

    private AnimationTimer gameLoop;
    private long lastFrameTime;
    private Image backgroundImage;
    private VBox pauseMenu;
    private javafx.scene.layout.StackPane pauseOverlay;
    private javafx.scene.layout.StackPane root;
    private BorderPane gameLayout;

    private static final double CANVAS_WIDTH = 700;
    private static final double CANVAS_HEIGHT = 600;
    private static final double BALL_SPEED = 300;
    private static final double PADDLE_SPEED = 400;

    public TwoPlayerGameScreen(Stage stage) {
        this.stage = stage;
        this.canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = new Image(getClass().getResourceAsStream("/images/backgroundGame.png"));
        } catch (Exception e) {
            logger.error("Could not load background image: {}", e.getMessage());
            backgroundImage = null;
        }
    }

    public void show() {
        // Create game layout with BorderPane
        gameLayout = new BorderPane();
        gameLayout.setCenter(canvas);
        gameLayout.setStyle("-fx-background-color: black;");

        initializeGame();

        // Add stats panel on right side
        statsPanel = new TwoPlayerStatsPanel(player1, player2);
        gameLayout.setRight(statsPanel);

        // Create root as StackPane to allow overlays
        root = new javafx.scene.layout.StackPane();
        root.getChildren().add(gameLayout);

        Scene scene = new Scene(root, CANVAS_WIDTH + 200, CANVAS_HEIGHT);
        setupInput(scene);

        startGameLoop();

        stage.setScene(scene);
        stage.setTitle("Arkanoid - Two Player Mode");
    }

    private void initializeGame() {
        // Load level bricks
        levelManager = new LevelManager();
        bricks = levelManager.loadLevel(0);
        logger.info("Loaded {} bricks for two-player mode", bricks.size());

        // Create Player 1 (bottom)
        Paddle paddle1 = new Paddle(CANVAS_WIDTH / 2 - 50, CANVAS_HEIGHT - 40, 100, 15, PADDLE_SPEED, 0, CANVAS_WIDTH);
        player1 = new Player("player1", 1, paddle1);
        Ball ball1 = new Ball(CANVAS_WIDTH / 2, CANVAS_HEIGHT - 60, 8, BALL_SPEED);
        ball1.setBounds(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        ball1.setTopIsDeadSide(false); // Bottom player: bottom is Dead Side
        player1.setBall(ball1);

        // Create Player 2 (top)
        Paddle paddle2 = new Paddle(CANVAS_WIDTH / 2 - 50, 25, 100, 15, PADDLE_SPEED, 0, CANVAS_WIDTH);
        player2 = new Player("player2", 2, paddle2);
        Ball ball2 = new Ball(CANVAS_WIDTH / 2, 45, 8, BALL_SPEED);
        ball2.setBounds(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        ball2.setTopIsDeadSide(true); // Top player: top is Dead Side
        player2.setBall(ball2);

        // Initialize services
        CollisionService collisionService = new CollisionServiceImpl(player1, player2);
        RespawnService respawnService = new RespawnServiceImpl(player1, player2, BALL_SPEED);
        PowerUpService powerUpService = new PowerUpServiceImpl(player1, player2);

        // Create match manager with brick count
        matchManager = new TwoPlayerMatchManagerImpl(player1, player2, collisionService, respawnService, powerUpService,
                bricks);

        // Initialize input handler with UI callbacks
        inputHandler = new TwoPlayerInputHandler(player1, player2, matchManager);
        inputHandler.setOnPauseCallback(this::showPauseMenu);
        inputHandler.setOnResumeCallback(this::hidePauseMenu);

        matchManager.startMatch();
        logger.info("Two-player game initialized: {} bricks, P1 at ({}, {}), P2 at ({}, {})",
            bricks.size(), paddle1.getX(), paddle1.getY(), paddle2.getX(), paddle2.getY());
    }

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            activeKeys.add(e.getCode());
            inputHandler.handleKeyPress(e.getCode());
        });
        scene.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
    }

    private void startGameLoop() {
        lastFrameTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                update(deltaTime);
                render();

                // Check for game over
                if (matchManager.getState() == TwoPlayerMatchManager.MatchState.GAME_OVER) {
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

        // Handle input through InputHandler
        inputHandler.handleContinuousInput(activeKeys, deltaTime);

        // Check ball-brick collisions and update bricks
        checkBrickCollisions();

        // Update bricks
        for (Brick brick : bricks) {
            brick.update(deltaTime);
        }

        // Update match
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
            
            // Check Player 1 ball collision
            if (ball1 != null && !ball1.isAttachedToPaddle() && brick.intersects(ball1)) {
                com.arkanoid.systems.sound.SoundManager.getInstance().playSound("brickBounce.wav");
                handleBallBrickCollision(ball1, brick);
                brick.hit();
                hitThisFrame = true;
                if (brick.isDestroyed()) {
                    logger.debug("Player 1 destroyed brick at ({}, {})", brick.getX(), brick.getY());
                    matchManager.applyBrickHit(1, 100); // Award points to player 1
                }
            }

            // Check Player 2 ball collision
            if (!hitThisFrame && ball2 != null && !ball2.isAttachedToPaddle() && brick.intersects(ball2)) {
                com.arkanoid.systems.sound.SoundManager.getInstance().playSound("brickBounce.wav");
                handleBallBrickCollision(ball2, brick);
                brick.hit();
                if (brick.isDestroyed()) {
                    logger.debug("Player 2 destroyed brick at ({}, {})", brick.getX(), brick.getY());
                    matchManager.applyBrickHit(2, 100); // Award points to player 2
                }
            }
        }
        
        // Remove destroyed bricks
        bricks.removeIf(Brick::isDestroyed);
    }

    private void handleBallBrickCollision(Ball ball, Brick brick) {
        double ballCenterX = ball.getCenterX();
        double ballCenterY = ball.getCenterY();
        double brickCenterX = brick.getX() + brick.getWidth() / 2;
        double brickCenterY = brick.getY() + brick.getHeight() / 2;

        double deltaX = ballCenterX - brickCenterX;
        double deltaY = ballCenterY - brickCenterY;

        double overlapX = (brick.getWidth() / 2 + ball.getWidth() / 2) - Math.abs(deltaX);
        double overlapY = (brick.getHeight() / 2 + ball.getHeight() / 2) - Math.abs(deltaY);

        if (overlapX < overlapY) {
            // Hit from side
            ball.reverseX();
            if (deltaX > 0) {
                ball.setX(brick.getX() + brick.getWidth());
            } else {
                ball.setX(brick.getX() - ball.getWidth());
            }
        } else {
            // Hit from top/bottom
            ball.reverseY();
            if (deltaY > 0) {
                ball.setY(brick.getY() + brick.getHeight());
            } else {
                ball.setY(brick.getY() - ball.getHeight());
            }
        }
    }

    private void render() {
        // Draw background image or fallback to black
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        }

        // Render bricks
        for (Brick brick : bricks) {
            if (!brick.isDestroyed()) {
                brick.render(gc);
            }
        }

        // Render paddles
        player1.getPaddle().render(gc);
        player2.getPaddle().render(gc);

        // Render balls
        if (player1.getBall() != null) {
            player1.getBall().render(gc);
        }
        if (player2.getBall() != null) {
            player2.getBall().render(gc);
        }

        // Update stats panel
        if (statsPanel != null) {
            statsPanel.update();
        }
    }

    // private void renderHUD() {
    //     gc.setFill(Color.WHITE);
    //     gc.setFont(Font.font("Arial", 16));

    //     gc.fillText("P1 Score: " + player1.getState().getScore(), 10, CANVAS_HEIGHT - 10);
    //     gc.fillText("P1 Lives: " + player1.getState().getLives(), 10, CANVAS_HEIGHT - 30);

    //     // Player 2 stats (top-left)
    //     gc.fillText("P2 Score: " + player2.getState().getScore(), 10, 20);
    //     gc.fillText("P2 Lives: " + player2.getState().getLives(), 10, 40);
    // }

    private void resumeGame() {
        matchManager.resume();
        hidePauseMenu();
    }

    private void showPauseMenu() {
        if (pauseMenu != null) {
            root.getChildren().remove(pauseMenu);
        }

        // Create overlay container
        javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane();
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

        // Title
        Text title = new Text("GAME PAUSED");
        title.setFont(Font.font("Arial Black", 42));
        title.setFill(Color.WHITE);
        title.setStroke(Color.CYAN);
        title.setStrokeWidth(2);

        // Resume Button
        Button resumeBtn = createMenuButton("Resume");
        resumeBtn.setOnAction(e -> resumeGame());

        // Restart Button
        Button restartBtn = createMenuButton("Restart");
        restartBtn.setOnAction(e -> restartGame());

        // Back to Menu Button
        Button backBtn = createMenuButton("Back to Menu");
        backBtn.setOnAction(e -> backToMenu());

        pauseMenu.getChildren().addAll(title, resumeBtn, restartBtn, backBtn);
        overlay.getChildren().add(pauseMenu);

        pauseOverlay = overlay;
        root.getChildren().add(pauseOverlay);
    }

    private void hidePauseMenu() {
        if (pauseOverlay != null) {
            root.getChildren().remove(pauseOverlay);
            pauseOverlay = null;
            pauseMenu = null;
        }
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);

        String normalStyle = "-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298);"
                + "-fx-text-fill: white;" + "-fx-font-size: 20px;" + "-fx-font-weight: bold;"
                + "-fx-background-radius: 15;" + "-fx-border-radius: 15;" + "-fx-border-color: white;"
                + "-fx-border-width: 2;" + "-fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: linear-gradient(to bottom right, #2a5298, #1e3c72);"
                + "-fx-text-fill: yellow;" + "-fx-font-size: 22px;" + "-fx-font-weight: bold;"
                + "-fx-background-radius: 15;" + "-fx-border-radius: 15;" + "-fx-border-color: white;"
                + "-fx-border-width: 2;" + "-fx-cursor: hand;";

        button.setStyle(normalStyle);
        button.setPrefWidth(250);
        button.setPrefHeight(50);

        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));

        return button;
    }

    private void restartGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        hidePauseMenu();

        initializeGame();
        startGameLoop();
    }

    private void backToMenu() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        hidePauseMenu();

        SceneManager.switchTo("mainMenuView");
    }

    private void showGameOver() {
        TwoPlayerMatchManagerImpl managerImpl = (TwoPlayerMatchManagerImpl) matchManager;

        logger.info("Two-player game ended: Winner={}, Reason={}, P1Score={}, P2Score={}",
            managerImpl.getWinningPlayer(), managerImpl.getEndReason(),
            player1.getState().getScore(), player2.getState().getScore());

        TwoPlayerGameOverScreen gameOverScreen = new TwoPlayerGameOverScreen(stage, managerImpl.getWinningPlayer(),
                managerImpl.getEndReason(), player1.getState().getScore(), player2.getState().getScore());

        gameOverScreen.show();
    }
}
