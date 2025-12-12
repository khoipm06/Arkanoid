package com.arkanoid.ui;

import com.arkanoid.core.entities.*;
import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.input.InputHandler;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.PlayerState;
import com.arkanoid.debug.MemoryMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.arkanoid.systems.save.GameSaveManager;
import com.arkanoid.systems.save.impl.GameSaveManagerImpl;
import com.arkanoid.ui.view.SaveLoadScene;
import com.arkanoid.ui.view.SceneManager;
import com.arkanoid.ui.view.SessionManager;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.effect.DropShadow;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class GameScene {
    private static final Logger logger = GameLogger.getLogger(GameScene.class);
    private final Scene scene;
    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final GameManager gameManager;
    private final InputHandler inputHandler;
    private final Set<KeyCode> pressedKeys;
    private final VBox pauseOverlay;
    private final StackPane root;
    private AnimationTimer gameLoop;
    private long lastUpdate;
    private Image backgroundImage;

    private Text scoreLabel;
    private Text livesLabel;
    private Text timeLabel;
    private String timeStr;
    
    // Performance monitoring (only visible in DEBUG mode)
    private Text fpsLabel;
    private HBox fpsBox;
    private int frameCount = 0;
    private long lastFpsUpdate = 0;
    private double currentFps = 0.0;
    private double averageFrameTime = 0.0;
    private VBox infoPanel; // Keep reference to add/remove FPS dynamically

    private GameScene(Stage stage, double width, double height, int levelNumber) {
        this.stage = stage;
        this.pressedKeys = new HashSet<>();

        double gameAreaWidth = 700;
        double gameAreaHeight = height;

        canvas = new Canvas(gameAreaWidth, gameAreaHeight);
        gc = canvas.getGraphicsContext2D();
        this.gameManager = GameManager.getInstance(canvas.getWidth(), canvas.getHeight(), levelNumber);
        this.inputHandler = new InputHandler(gameManager);

        // Enable memory monitoring
        MemoryMonitor.setEnabled(true);
        MemoryMonitor.getInstance().trackGameManager(gameManager);

        try (InputStream streamBackGround = getClass().getResourceAsStream("/images/backgroundGame.png")) {
            if (streamBackGround != null) {
                this.backgroundImage = new Image(streamBackGround);
            } else {
                logger.warn("Background warning: Cannot find background.png file. Using black background.");
            }
        } catch (Exception e) {
            logger.error("Failed to load game background image resource: {}", e.getMessage(), e);
        }

        BorderPane mainLayout = new BorderPane();

        this.infoPanel = createInfoPanel();

        // Put into BorderPane
        mainLayout.setCenter(canvas);
        mainLayout.setRight(infoPanel);

        // Pause overlay
        pauseOverlay = createPauseOverlay();
        pauseOverlay.setVisible(false);

        // Overlay over the root StackPane
        root = new StackPane(mainLayout, pauseOverlay);
        double infoWidth = 350;
        this.scene = new Scene(root, canvas.getWidth() + infoWidth, height);

        setupInputHandlers();
        stage.setResizable(false);
        stage.setScene(scene);

    }

    public static GameScene getInstance(Stage stage, double width, double height, int levelNumber) {
        return new GameScene(stage, width, height, levelNumber);
    }

    public void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }
    }
    


    private void openSaveLoadScene() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/arkanoid/ui/view/SaveLoadView.fxml"));
            StackPane saveLoadRoot = loader.load();
            SaveLoadScene controller = loader.getController();
            int userId = SessionManager.getCurrentUser() != null 
                ? SessionManager.getCurrentUser().getId() 
                : 0; // Fallback to 0 if not logged in
            logger.debug("Opening save/load scene for userId: {}", userId);
            GameSaveManager gameSaveManager = new GameSaveManagerImpl(gameManager);
            Scene saveLoadScene = new Scene(saveLoadRoot);
            saveLoadScene.getStylesheets().add(
                    getClass().getResource("/com/arkanoid/ui/saveload.css").toExternalForm());
            controller.init(
                    gameSaveManager,
                    gameManager,
                    stage,
                    userId,
                    () -> {
                        stage.setScene(scene);
                        pauseOverlay.setVisible(true);
                    },
                    saveLoadRoot,
                    this);
            stage.setScene(saveLoadScene);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to open Save/Load scene: {}", e.getMessage());
        }
    }

    private VBox createInfoPanel() {
        VBox info = new VBox(20);
        info.setAlignment(Pos.TOP_CENTER);
        info.setPrefWidth(350);

        info.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1a1a2e, #162447); " + // Dark blue-purple gradient background
                        "-fx-padding: 30; " +
                        "-fx-border-color: #00ffff; " + // Bright cyan border for visibility
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15;");

        // Game Title
        Text title = new Text("ARKANOID");
        title.setFont(Font.font("Arial Black", 32));
        title.setFill(Color.CYAN);
        title.setStroke(Color.DARKBLUE);
        title.setStrokeWidth(2);

        // Score
        HBox scoreBox = createInfoRow("SCORE", "0", Color.LIMEGREEN);
        scoreLabel = (Text) scoreBox.getChildren().get(1);

        // LIVES
        HBox livesBox = createInfoRow("LIVES", "3", Color.TOMATO);
        livesLabel = (Text) livesBox.getChildren().get(1);

        // LEVEL
        HBox levelBox = createInfoRow("LEVEL", String.valueOf(gameManager.getLevelNumber()), Color.GOLD);

        // TIME
        HBox timeBox = createInfoRow("TIME", "00:00", Color.LIGHTBLUE);
        timeLabel = (Text) timeBox.getChildren().get(1);

        // FPS (Performance monitoring - dynamically shown in DEBUG/TRACE mode)
        fpsBox = createInfoRow("FPS", "60", Color.ORANGE);
        fpsLabel = (Text) fpsBox.getChildren().get(1);

        // Add base info
        info.getChildren().addAll(title, scoreBox, livesBox, levelBox, timeBox);
        
        // Add FPS if debug level is active
        if (isDebugMode()) {
            info.getChildren().add(fpsBox);
            logger.debug("FPS counter enabled (DEBUG/TRACE mode active)");
        }

        // Shadow effect to make panel stand out
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 255, 255, 0.6)); // Semi-transparent cyan glow effect
        shadow.setRadius(15);
        info.setEffect(shadow);

        return info;
    }

    private HBox createInfoRow(String label, String value, Color valueColor) {
        Text lbl = new Text(label + ":");
        lbl.setFont(Font.font("Consolas", 18));
        lbl.setFill(Color.LIGHTGRAY);

        Text val = new Text(value);
        val.setFont(Font.font("Consolas", 22));
        val.setFill(valueColor);

        HBox row = new HBox(10, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox createPauseOverlay() {
        VBox overlay = new VBox(30);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);"
                + "-fx-padding: 40;"
                + "-fx-border-radius: 20;"
                + "-fx-background-radius: 20;");
        overlay.setPrefSize(canvas.getWidth() * 0.8, canvas.getHeight() * 0.6);

        Text pausedText = new Text("PAUSED");
        pausedText.setFont(Font.font("Arial Black", 60));
        pausedText.setFill(Color.WHITE);
        pausedText.setStroke(Color.LIGHTBLUE);
        pausedText.setStrokeWidth(2);

        Button resumeBtn = new Button("Resume");
        stylePauseButton(resumeBtn);
        resumeBtn.setOnAction(e -> {
            gameManager.resume();
            pauseOverlay.setVisible(false);
        });

        Button gameSavesBtn = new Button("Game Saves");
        stylePauseButton(gameSavesBtn);
        gameSavesBtn.setOnAction(e -> {
            openSaveLoadScene();
        });

        Button newGameBtn = new Button("New Game");
        stylePauseButton(newGameBtn);
        newGameBtn.setOnAction(e -> {
            GameScene newScene = GameScene.getInstance(stage, stage.getWidth(), stage.getHeight(),
                    gameManager.getLevelNumber());
            newScene.start();
            stage.setScene(newScene.getScene());
        });

        Button quitBtn = new Button("Quit");
        stylePauseButton(quitBtn);
        quitBtn.setOnAction(e -> {
            SceneManager.switchTo("mainMenuView");
        });

        overlay.getChildren().addAll(pausedText, resumeBtn, gameSavesBtn, newGameBtn, quitBtn);
        return overlay;
    }

    private void stylePauseButton(Button button) {
        button.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 15;"
                + "-fx-border-radius: 15;"
                + "-fx-border-color: white;"
                + "-fx-border-width: 2;"
                + "-fx-cursor: hand;");
        button.setPrefWidth(200);
        button.setPrefHeight(50);

        button.setOnMouseEntered(
                e -> button.setStyle("-fx-background-color: linear-gradient(to bottom right, #2a5298, #1e3c72);"
                        + "-fx-text-fill: yellow;"
                        + "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 15;"
                        + "-fx-border-radius: 15;"
                        + "-fx-border-color: white;"
                        + "-fx-border-width: 2;"
                        + "-fx-cursor: hand;"));
        button.setOnMouseExited(e -> stylePauseButton(button));
    }

    private void setupInputHandlers() {
        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if (inputHandler.handleKeyPress(key)) {
                pauseOverlay.setVisible(gameManager.getCurrentState() == GameManager.GameState.PAUSED);
                return;
            }
            // F3 - Increase log level (less verbose)
            if (key == KeyCode.F3) {
                increaseLogLevel();
                return;
            }
            // F4 - Decrease log level (more verbose)
            if (key == KeyCode.F4) {
                decreaseLogLevel();
                return;
            }
            // F5 - Quick save
            if (key == KeyCode.F5) {
                if (gameManager.getCurrentState() == GameManager.GameState.PAUSED) {
                    try {
                        GameSaveManagerImpl gameSaveManager = new GameSaveManagerImpl(gameManager);
                        int userId = SessionManager.getCurrentUser() != null 
                            ? SessionManager.getCurrentUser().getId() 
                            : 0;
                        logger.debug("Quick save initiated via F5 key for user ID: {}", userId);
                        WritableImage snapshot = captureCanvasSnapshot();
                        
                        // Use async save
                        gameSaveManager.saveCurrentGameWithAutoNameAsync(userId, snapshot)
                            .thenAccept(savedGame -> {
                                logger.info("Quick save completed successfully - Save ID: {}, Name: '{}'", 
                                           savedGame.getId(), savedGame.getSaveName());
                            })
                            .exceptionally(ex -> {
                                logger.error("Quick save operation failed: {}", ex.getMessage(), ex);
                                return null;
                            });
                    } catch (Exception e) {
                        logger.error("Quick save initialization failed: {}", e.getMessage(), e);
                    }
                }
                return;
            }
            // Add movement keys to continuous input set
            pressedKeys.add(key);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });
    }

    public void start() {
        gameManager.startGame();
        lastFpsUpdate = lastUpdate = System.nanoTime();
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                
                // Update FPS counter every second (dynamically check log level)
                boolean isDebug = isDebugMode();
                frameCount++;
                long timeSinceLastFpsUpdate = now - lastFpsUpdate;
                if (timeSinceLastFpsUpdate >= 1_000_000_000L) {
                    if (isDebug) {
                        currentFps = frameCount / (timeSinceLastFpsUpdate / 1_000_000_000.0);
                        averageFrameTime = (timeSinceLastFpsUpdate / 1_000_000.0) / frameCount;
                        if (!infoPanel.getChildren().contains(fpsBox)) {
                            infoPanel.getChildren().add(fpsBox);
                        }
                        fpsLabel.setText(String.format("%.0f (%.1fms)", currentFps, averageFrameTime));
                        // Color code FPS: green (60+), yellow (45-60), red (<45)
                        if (currentFps >= 60) {
                            fpsLabel.setFill(Color.LIMEGREEN);
                        } else if (currentFps >= 45) {
                            fpsLabel.setFill(Color.YELLOW);
                        } else {
                            fpsLabel.setFill(Color.RED);
                        }
                    } else {
                        // Hide FPS box if debug mode is off
                        infoPanel.getChildren().remove(fpsBox);
                    }
                    frameCount = 0;
                    lastFpsUpdate = now;
                }
                
                inputHandler.handleContinuousInput(pressedKeys, deltaTime);
                update(deltaTime);
                render();
            }
        };
        gameLoop.start();
    }

    private void update(double deltaTime) {
        gameManager.update(deltaTime);
        if (gameManager.getPlayer() != null) {
            PlayerState state = gameManager.getPlayer().getState();
            Platform.runLater(() -> {
                scoreLabel.setText(String.valueOf(state.getScore()));
                livesLabel.setText(String.valueOf(state.getLives()));
            });
        }
        int elapsed = (int) gameManager.getElapsedTimeSeconds();
        int minutes = elapsed / 60;
        int seconds = elapsed % 60;
        timeStr = String.format("%02d:%02d", minutes, seconds);
        Platform.runLater(() -> timeLabel.setText(timeStr));
        if (gameManager.getCurrentState() == GameManager.GameState.GAME_OVER) {
            gameLoop.stop();
            Platform.runLater(() -> {
                SceneManager.showGameOver(
                        gameManager.getLevelNumber(),
                        gameManager.getScore(),
                        timeStr);
            });
            return;
        }

        if (gameManager.getCurrentState() == GameManager.GameState.LEVEL_COMPLETE) {
            gameLoop.stop();
            Platform.runLater(() -> {
                SceneManager.showWinLevel(
                        gameManager.getLevelNumber(),
                        gameManager.getScore(),
                        timeStr);
            });
        }
    }

    private void render() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        for (Brick brick : gameManager.getBricks()) {
            brick.render(gc);
        }

        for (Ball ball : gameManager.getBalls()) {
            ball.render(gc);
        }

        for (PowerUp powerUp : gameManager.getPowerUps()) {
            powerUp.render(gc);
        }
        for (Bullet bullet : gameManager.getBullets()) {
            bullet.render(gc);
        }

        for (Explosion explosion : gameManager.getExplosions()) {
            explosion.render(gc);
        }

        for (Particle particle : gameManager.getParticles()) {
            particle.render(gc);
        }

        for (LineEffect lineEffect : gameManager.getLineEffects()) {
            lineEffect.render(gc);
        }
        for (TrailEffect trailEffect : gameManager.getTrailEffects()) {
            trailEffect.render(gc);
        }
        for (FloatingText floatingText : gameManager.getFloatingTexts()) {
            floatingText.render(gc);
        }
        if (gameManager.getPlayer() != null) {
            gameManager.getPlayer().getPaddle().render(gc);
        }

    }

    public Scene getScene() {
        return scene;
    }

    /**
     * Get the game canvas for thumbnail capture.
     * 
     * @return The game canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Captures a snapshot of the current game canvas as WritableImage.
     * Must be called on JavaFX Application Thread.
     * 
     * @return WritableImage snapshot of the canvas
     */
    public WritableImage captureCanvasSnapshot() {
        WritableImage snapshot = new WritableImage(
                (int) canvas.getWidth(),
                (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        return snapshot;
    }

    /**
     * Check if current log level is DEBUG or TRACE.
     */
    private boolean isDebugMode() {
        return logger.isDebugEnabled() || logger.isTraceEnabled();
    }

    /**
     * Decrease log level (make more verbose): ERROR -> WARN -> INFO -> DEBUG -> TRACE
     */
    private void decreaseLogLevel() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Level currentLevel = rootLogger.getLevel();
        Level newLevel = currentLevel;
        
        if (currentLevel == Level.ERROR) {
            newLevel = Level.WARN;
        } else if (currentLevel == Level.WARN) {
            newLevel = Level.INFO;
        } else if (currentLevel == Level.INFO) {
            newLevel = Level.DEBUG;
        } else if (currentLevel == Level.DEBUG) {
            newLevel = Level.TRACE;
        }
        
        if (newLevel != currentLevel) {
            com.arkanoid.systems.logging.LoggingManager.getInstance().setLogLevel(newLevel);
            logger.info("[F3] Log level decreased: {} -> {}", currentLevel, newLevel);
        } else {
            logger.info("[F3] Already at most verbose level: {}", currentLevel);
        }
    }

    /**
     * Increase log level (make less verbose): TRACE -> DEBUG -> INFO -> WARN -> ERROR
     */
    private void increaseLogLevel() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Level currentLevel = rootLogger.getLevel();
        Level newLevel = currentLevel;
        
        if (currentLevel == Level.TRACE) {
            newLevel = Level.DEBUG;
        } else if (currentLevel == Level.DEBUG) {
            newLevel = Level.INFO;
        } else if (currentLevel == Level.INFO) {
            newLevel = Level.WARN;
        } else if (currentLevel == Level.WARN) {
            newLevel = Level.ERROR;
        }
        
        if (newLevel != currentLevel) {
            com.arkanoid.systems.logging.LoggingManager.getInstance().setLogLevel(newLevel);
            logger.info("[F4] Log level increased: {} -> {}", currentLevel, newLevel);
        } else {
            logger.info("[F4] Already at least verbose level: {}", currentLevel);
        }
    }
}
