package com.arkanoid;

import com.arkanoid.core.GameConstants;
import com.arkanoid.database.DatabaseManager;
import com.arkanoid.debug.MemoryMonitor;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.logging.LoggingManager;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.systems.threading.ThreadManager;
import com.arkanoid.ui.view.SceneManager;
import com.arkanoid.ui.view.SessionManager;
import com.arkanoid.utils.CommandLineArgs;
import com.arkanoid.utils.ImageLoader;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameApplication extends Application {
    private static final Logger logger = GameLogger.getLogger(GameApplication.class);
    private static final SoundManager soundManager = SoundManager.getInstance();
    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();
    private static final AtomicBoolean shutdownExecuted = new AtomicBoolean(false);

    public static final double GAME_WIDTH = GameConstants.WINDOW_WIDTH;
    public static final double GAME_HEIGHT = GameConstants.WINDOW_HEIGHT;

    @Override
    public void start(Stage primaryStage) throws IOException {
        logger.info("JavaFX Application starting...");

        java.util.logging.Logger.getLogger("javafx.scene.CssStyleHelper").setLevel(java.util.logging.Level.SEVERE);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                logger.error("Uncaught exception in thread {}: {}", thread.getName(), throwable.getMessage(), throwable));

        logger.info("Initializing database...");
        databaseManager.initialize();

        logger.info("Checking for saved user session...");
        boolean restoredSession = SessionManager.restoreSession();
        if (restoredSession) {
            logger.info("User session restored successfully");
        } else {
            logger.info("No saved session found");
        }

        logger.info("Loading scenes...");
        SceneManager.setStage(primaryStage);
        SceneManager.loadScene("mainMenuView", "MainMenuView.fxml");
        SceneManager.loadScene("shopView", "ShopView.fxml");
        SceneManager.loadScene("settingView", "SettingView.fxml");
        SceneManager.loadScene("profileScreen", "ProfileScreen.fxml");
        SceneManager.loadScene("authScreen", "AuthScreen.fxml");
        SceneManager.loadScene("signIn", "SignIn.fxml");
        SceneManager.loadScene("signUpView", "SignUpView.fxml");
        SceneManager.loadScene("map", "Map.fxml");
        SceneManager.loadScene("shopBall", "ShopBall.fxml");
        SceneManager.loadScene("shopPaddle", "ShopPaddle.fxml");
        SceneManager.loadScene("gameOver", "GameOver.fxml");
        SceneManager.loadScene("winLevel", "WinLevel.fxml");
        SceneManager.loadScene("leaderboard", "Leaderboard.fxml");

        logger.info("Switching to main menu...");
        SceneManager.switchTo("mainMenuView");

        Image appIcon = ImageLoader.loadImage("/icons/arkanoid.png");
        if (appIcon != null) {
            primaryStage.getIcons().add(appIcon);
        } else {
            logger.warn("Application icon /icons/arkanoid.png could not be loaded");
        }

        primaryStage.setTitle("Arkanoid Game");
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();

        logger.info("Starting background music...");
        soundManager.playBackground("background.mp3", true);

        logger.info("Application started successfully");
    }

    @Override
    public void stop() {
        shutdownResources();
    }

    private static void shutdownResources() {
        if (!shutdownExecuted.compareAndSet(false, true)) {
            return;
        }
        logger.info("Application shutting down...");
        try {
            SceneManager.cleanup();
        } catch (Exception e) {
            logger.error("Error cleaning up SceneManager: {}", e.getMessage(), e);
        }
        try {
            SoundManager.getInstance().cleanup();
        } catch (Exception e) {
            logger.error("Error cleaning up SoundManager: {}", e.getMessage(), e);
        }
        try {
            MemoryMonitor.getInstance().shutdown();
        } catch (Exception e) {
            logger.error("Error shutting down MemoryMonitor: {}", e.getMessage(), e);
        }
        try {
            ThreadManager.getInstance().shutdown();
        } catch (Exception e) {
            logger.error("Error shutting down ThreadManager: {}", e.getMessage(), e);
        }
        try {
            DatabaseManager.getInstance().close();
        } catch (Exception e) {
            logger.error("Error closing DatabaseManager: {}", e.getMessage(), e);
        }
        LoggingManager.getInstance().shutdown();
    }

    public static void switchToProfileOrAuth() {
        if (SessionManager.isLoggedIn()) {
            SceneManager.switchTo("profileScreen");
        } else {
            SceneManager.switchTo("authScreen");
        }
    }

    public static void main(String[] args) {
        CommandLineArgs.Config config = CommandLineArgs.parse(args);
        config.applyEnvironmentVariables();
        LoggingManager.getInstance().initialize(config.getLoggingConfig());

        Runtime.getRuntime().addShutdownHook(new Thread(GameApplication::shutdownResources, "AppShutdownHook"));

        logger.info("=== Arkanoid Game Starting ===");
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("JavaFX Version: {}", System.getProperty("javafx.version"));
        logger.info("Log Level: {}", config.getLoggingConfig().getLogLevel());

        launch(args);
    }
}
