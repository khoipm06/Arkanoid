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

import java.io.IOException;

public class GameApplication extends Application {
    private static final SoundManager soundManager = SoundManager.getInstance();
    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();
    public static final double GAME_WIDTH = 1000;
    public static final double GAME_HEIGHT = 650;

    @Override
    public void start(Stage primaryStage) throws IOException {
        GameLogger.info("JavaFX Application starting...");
        
        // Set up uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            GameLogger.error("Uncaught exception in thread {}: {}", thread.getName(), throwable.getMessage(), throwable);
        });
        
        GameLogger.info("Initializing database...");
        databaseManager.initialize();
        
        GameLogger.info("Loading scenes...");
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

        GameLogger.info("Switching to main menu...");
        SceneManager.switchTo("mainMenuView");

        try {
            primaryStage.getIcons()
                    .add(new Image(getClass().getResourceAsStream("/icons/arkanoid.png")));
        } catch (Exception e) {
            GameLogger.error("Error loading application icon", e);
        }

        primaryStage.setTitle("Arkanoid Game");
        primaryStage.show();

        GameLogger.info("Starting background music...");
        soundManager.playBackground("background.mp3", true);
        
        GameLogger.info("Application started successfully");
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
            GameLogger.info("Application shutting down...");
            LoggingManager.getInstance().shutdown();
        }));

        GameLogger.info("=== Arkanoid Game Starting ===");
        GameLogger.info("Java Version: {}", System.getProperty("java.version"));
        GameLogger.info("JavaFX Version: {}", System.getProperty("javafx.version"));
        GameLogger.info("Log Level: {}", config.getLoggingConfig().getLogLevel());


        launch(args);
    }
}
