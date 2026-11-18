package com.arkanoid.ui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.arkanoid.core.GameConstants;
import com.arkanoid.core.entities.*;
import com.arkanoid.debug.MemoryMonitor;
import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.input.InputHandler;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.logging.LoggingManager;
import com.arkanoid.systems.player.PlayerState;
import com.arkanoid.systems.save.GameSaveManager;
import com.arkanoid.systems.save.impl.GameSaveManagerImpl;
import com.arkanoid.ui.view.SaveLoadScene;
import com.arkanoid.ui.view.SceneLifecycle;
import com.arkanoid.ui.view.SceneManager;
import com.arkanoid.ui.view.SessionManager;
import com.arkanoid.ui.view.ViewportScaler;
import com.arkanoid.utils.ImageLoader;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Single-player gameplay view rendered inside {@link SceneManager}'s persistent {@code 1000 x 650} {@link Scene}.
 */
public class GameScene implements SceneLifecycle {
    private static final Logger logger = GameLogger.getLogger(GameScene.class);

    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final GameManager gameManager;
    private final InputHandler inputHandler;
    private final Set<KeyCode> pressedKeys;
    private final GameHudPanel hudPanel;
    private final GamePauseOverlay pauseOverlay;
    private final StackPane root;
    private final Image backgroundImage;

    private AnimationTimer gameLoop;
    private long lastUpdate = 0L;
    private int frameCount = 0;
    private long lastFpsUpdate = 0L;
    private int displayLevel;
    private boolean endTransitionTriggered = false;
    private boolean cleanedUp = false;

    private StackPane saveLoadOverlay;
    private SaveLoadScene saveLoadController;

    private GameScene(Stage stage, double width, double height, int levelNumber) {
        this.stage = stage != null ? stage : SceneManager.getStage();
        this.displayLevel = levelNumber;
        this.pressedKeys = new HashSet<>();

        double gameAreaWidth = GameConstants.SINGLE_PLAYER_CANVAS_WIDTH;
        double gameAreaHeight = GameConstants.WINDOW_HEIGHT;

        this.canvas = new Canvas(gameAreaWidth, gameAreaHeight);
        this.gc = canvas.getGraphicsContext2D();
        this.gameManager = GameManager.getInstance(canvas.getWidth(), canvas.getHeight(), levelNumber);
        this.inputHandler = new InputHandler(gameManager);
        this.backgroundImage = ImageLoader.loadImage("/images/backgroundGame.png");

        this.hudPanel = new GameHudPanel(levelNumber, isDebugMode());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(canvas);
        mainLayout.setRight(hudPanel);

        this.pauseOverlay = new GamePauseOverlay(
                canvas.getWidth(),
                canvas.getHeight(),
                this::resumeFromPause,
                this::openSaveLoadScene,
                this::startNewGame,
                this::quitToMenu
        );
        this.pauseOverlay.setVisible(false);

        StackPane contentRoot = new StackPane(mainLayout, pauseOverlay);
        this.root = ViewportScaler.wrap(contentRoot, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
    }

    public static GameScene getInstance(Stage stage, double width, double height, int levelNumber) {
        return new GameScene(stage, width, height, levelNumber);
    }

    @Override
    public void onEnter() {
        MemoryMonitor.setEnabled(true);
        MemoryMonitor.getInstance().trackGameManager(gameManager);
    }

    @Override
    public void onExit() {
        cleanup();
    }

    public void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }
    }

    /**
     * Stops the game loop, clears input state, and releases simulation and monitor references.
     */
    public void cleanup() {
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;
        logger.info("GameScene cleanup started");

        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
            logger.info("AnimationTimer stopped");
        }

        closeSaveLoadOverlay(false);
        pressedKeys.clear();
        gameManager.cleanup();

        MemoryMonitor.getInstance().untrackGameManager();
        MemoryMonitor.setEnabled(false);

        logger.info("GameScene cleanup completed");
    }

    private void resumeFromPause() {
        lastUpdate = 0L;
        pressedKeys.clear();
        gameManager.resume();
        pauseOverlay.setVisible(false);
    }

    private void startNewGame() {
        GameScene newScene = GameScene.getInstance(
                stage != null ? stage : SceneManager.getStage(),
                GameConstants.WINDOW_WIDTH,
                GameConstants.WINDOW_HEIGHT,
                displayLevel
        );
        newScene.start();
    }

    private void quitToMenu() {
        SceneManager.switchTo("mainMenuView");
    }

    private void openSaveLoadScene() {
        if (saveLoadOverlay != null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/arkanoid/ui/view/SaveLoadView.fxml"));
            StackPane saveLoadRoot = loader.load();
            URL cssUrl = getClass().getResource("/com/arkanoid/ui/saveload.css");
            if (cssUrl != null) {
                saveLoadRoot.getStylesheets().add(cssUrl.toExternalForm());
            }

            SaveLoadScene controller = loader.getController();
            int userId = SessionManager.getCurrentUser() != null
                    ? SessionManager.getCurrentUser().getId()
                    : 0;
            logger.debug("Opening save/load overlay for userId: {}", userId);

            GameSaveManager gameSaveManager = new GameSaveManagerImpl(gameManager);
            StackPane scaledSaveLoad = ViewportScaler.wrap(saveLoadRoot, 800, 600);

            this.saveLoadOverlay = scaledSaveLoad;
            this.saveLoadController = controller;

            controller.init(
                    gameSaveManager,
                    gameManager,
                    stage,
                    userId,
                    () -> closeSaveLoadOverlay(true),
                    saveLoadRoot,
                    this
            );

            pauseOverlay.setVisible(false);
            root.getChildren().add(scaledSaveLoad);
        } catch (Exception e) {
            logger.error("Failed to open Save/Load overlay: {}", e.getMessage(), e);
        }
    }

    private void closeSaveLoadOverlay(boolean restorePauseOverlay) {
        if (saveLoadController != null) {
            saveLoadController.cleanup();
            saveLoadController = null;
        }
        if (saveLoadOverlay != null) {
            root.getChildren().remove(saveLoadOverlay);
            saveLoadOverlay = null;
        }
        if (restorePauseOverlay && !cleanedUp) {
            pauseOverlay.setVisible(true);
        }
    }

    /**
     * Refreshes HUD and canvas immediately after {@link SaveLoadScene} restores a saved game.
     */
    public void onSaveLoaded(int loadedLevel) {
        this.displayLevel = loadedLevel;
        this.lastUpdate = 0L;
        this.endTransitionTriggered = false;
        syncHudFromManager();
        render();
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode key = event.getCode();
        if (saveLoadOverlay != null) {
            if (key == KeyCode.ESCAPE) {
                closeSaveLoadOverlay(true);
            }
            return;
        }

        if (inputHandler.handleKeyPress(key)) {
            boolean paused = gameManager.getCurrentState() == GameManager.GameState.PAUSED;
            pauseOverlay.setVisible(paused);
            if (!paused) {
                lastUpdate = 0L;
                pressedKeys.clear();
            }
            return;
        }

        if (key == KeyCode.F3) {
            increaseLogLevel();
            return;
        }
        if (key == KeyCode.F4) {
            decreaseLogLevel();
            return;
        }
        if (key == KeyCode.F5) {
            handleQuickSave();
            return;
        }

        pressedKeys.add(key);
    }

    private void handleKeyReleased(KeyEvent event) {
        pressedKeys.remove(event.getCode());
    }

    private void handleQuickSave() {
        if (gameManager.getCurrentState() != GameManager.GameState.PAUSED) {
            return;
        }
        try {
            GameSaveManagerImpl gameSaveManager = new GameSaveManagerImpl(gameManager);
            int userId = SessionManager.getCurrentUser() != null
                    ? SessionManager.getCurrentUser().getId()
                    : 0;
            logger.debug("Quick save initiated via F5 key for user ID: {}", userId);
            WritableImage snapshot = captureCanvasSnapshot();

            gameSaveManager.saveCurrentGameWithAutoNameAsync(userId, snapshot)
                    .thenAccept(savedGame -> logger.info(
                            "Quick save completed successfully - Save ID: {}, Name: '{}'",
                            savedGame.getId(), savedGame.getSaveName()))
                    .exceptionally(ex -> {
                        logger.error("Quick save operation failed: {}", ex.getMessage(), ex);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Quick save initialization failed: {}", e.getMessage(), e);
        }
    }

    public void start() {
        SceneManager.showCustomRoot("game", root, this);
        SceneManager.setKeyHandlers(this::handleKeyPressed, this::handleKeyReleased);

        gameManager.startGame();
        lastUpdate = 0L;
        lastFpsUpdate = 0L;
        frameCount = 0;
        endTransitionTriggered = false;
        cleanedUp = false;
        syncHudFromManager();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    lastFpsUpdate = now;
                    return;
                }

                double deltaTime = Math.min(
                        Math.max((now - lastUpdate) / 1_000_000_000.0, 0.0),
                        GameConstants.MAX_FRAME_DELTA
                );
                lastUpdate = now;

                updateFpsCounter(now);
                inputHandler.handleContinuousInput(pressedKeys, deltaTime);
                update(deltaTime);
                render();
            }
        };
        gameLoop.start();
    }

    private void updateFpsCounter(long now) {
        boolean isDebug = isDebugMode();
        frameCount++;
        long elapsedNanos = now - lastFpsUpdate;
        if (elapsedNanos >= 1_000_000_000L) {
            double currentFps = frameCount / (elapsedNanos / 1_000_000_000.0);
            double averageFrameTime = (elapsedNanos / 1_000_000.0) / frameCount;
            hudPanel.updateFps(isDebug, currentFps, averageFrameTime);
            frameCount = 0;
            lastFpsUpdate = now;
        }
    }

    private void update(double deltaTime) {
        gameManager.update(deltaTime);
        syncHudFromManager();

        if (endTransitionTriggered) {
            return;
        }

        GameManager.GameState state = gameManager.getCurrentState();
        if (state == GameManager.GameState.GAME_OVER) {
            endTransitionTriggered = true;
            if (gameLoop != null) {
                gameLoop.stop();
            }
            SceneManager.showGameOver(
                    displayLevel,
                    gameManager.getScore(),
                    hudPanel.getFormattedTime()
            );
            return;
        }

        if (state == GameManager.GameState.LEVEL_COMPLETE) {
            endTransitionTriggered = true;
            if (gameLoop != null) {
                gameLoop.stop();
            }
            SceneManager.showWinLevel(
                    displayLevel,
                    gameManager.getScore(),
                    hudPanel.getFormattedTime()
            );
        }
    }

    private void syncHudFromManager() {
        if (gameManager.getPlayer() != null) {
            PlayerState state = gameManager.getPlayer().getState();
            hudPanel.updateStats(
                    state.getScore(),
                    state.getLives(),
                    displayLevel,
                    (int) gameManager.getElapsedTimeSeconds()
            );
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

        gameManager.getVisualEffectsManager().render(gc);

        if (gameManager.getPlayer() != null) {
            gameManager.getPlayer().getPaddle().render(gc);
        }
    }

    public Scene getScene() {
        return SceneManager.getMainScene();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Captures a snapshot of the current game canvas as WritableImage.
     * Must be called on JavaFX Application Thread.
     */
    public WritableImage captureCanvasSnapshot() {
        WritableImage snapshot = new WritableImage(
                (int) canvas.getWidth(),
                (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        return snapshot;
    }

    private boolean isDebugMode() {
        return logger.isDebugEnabled() || logger.isTraceEnabled();
    }

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
            LoggingManager.getInstance().setLogLevel(newLevel);
            logger.info("[F4] Log level decreased: {} -> {}", currentLevel, newLevel);
        } else {
            logger.info("[F4] Already at most verbose level: {}", currentLevel);
        }
    }

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
            LoggingManager.getInstance().setLogLevel(newLevel);
            logger.info("[F3] Log level increased: {} -> {}", currentLevel, newLevel);
        } else {
            logger.info("[F3] Already at least verbose level: {}", currentLevel);
        }
    }
}
