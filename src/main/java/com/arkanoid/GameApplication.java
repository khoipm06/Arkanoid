package com.arkanoid;

import com.arkanoid.database.DatabaseManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.logging.LoggingManager;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.view.SceneManager;
import com.arkanoid.ui.view.SessionManager;
import com.arkanoid.utils.CommandLineArgs;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class GameApplication extends Application {
    private static final Logger logger = GameLogger.getLogger(GameApplication.class);
    private static final SoundManager soundManager = SoundManager.getInstance();
    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();
    public static final double GAME_WIDTH = 1000;
    public static final double GAME_HEIGHT = 650;

    @Override
    public void start(Stage primaryStage) throws IOException {
        logger.info("JavaFX Application starting...");

        // Set up uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread {}: {}", thread.getName(), throwable.getMessage(), throwable);
        });

        logger.info("Initializing database...");
        databaseManager.initialize();

        // Restore saved user session if exists
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

        try (InputStream iconStream = (InputStream) Objects.requireNonNull(
                             getClass().getResourceAsStream("/icons/arkanoid.png"))) {
            primaryStage.getIcons().add(new Image(iconStream));
        } catch (Exception e) {
            logger.error("Error loading application icon", e);
        }

        primaryStage.setTitle("Arkanoid Game");
        primaryStage.show();

        logger.info("Starting background music...");
        soundManager.playBackground("background.mp3", true);

        logger.info("Application started successfully");
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutting down...");
            LoggingManager.getInstance().shutdown();
        }));

        logger.info("=== Arkanoid Game Starting ===");
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("JavaFX Version: {}", System.getProperty("javafx.version"));
        logger.info("Log Level: {}", config.getLoggingConfig().getLogLevel());


        launch(args);
    }
}
