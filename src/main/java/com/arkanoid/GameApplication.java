package com.arkanoid;

import com.arkanoid.database.DatabaseManager;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.view.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApplication extends Application {
    public static final double GAME_WIDTH = 1000;
    public static final double GAME_HEIGHT = 650;

    @Override
    public void start(Stage primaryStage) throws IOException {
        DatabaseManager.initialize();
        SceneManager.setStage(primaryStage);
        SceneManager.loadScene("mainMenuView", "MainMenuView.fxml");
        SceneManager.loadScene("modeSelectView", "ModeSelectView.fxml");
        SceneManager.loadScene("shopView", "ShopView.fxml");
        SceneManager.loadScene("settingView", "SettingView.fxml");
        SceneManager.loadScene("profileScreen", "ProfileScreen.fxml");
        SceneManager.loadScene("authScreen", "AuthScreen.fxml");
        SceneManager.loadScene("signIn", "SignIn.fxml");
        SceneManager.loadScene("signUpView", "SignUpView.fxml");
        SceneManager.loadScene("map", "Map.fxml");
        SceneManager.loadScene("pause", "Pause.fxml");
        SceneManager.loadScene("shopBall", "ShopBall.fxml");
        SceneManager.loadScene("shopPaddle", "ShopPaddle.fxml");
        SceneManager.loadScene("gameOver", "GameOver.fxml");
        SceneManager.loadScene("winLevel", "WinLevel.fxml");
        SceneManager.loadScene("leaderboard", "Leaderboard.fxml");

        SceneManager.switchTo("mainMenuView");

        try {
            primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/arkanoid.png")));
        } catch (Exception e) {
            System.err.println("Error loading application icon: " + e.getMessage());
        }

        primaryStage.setTitle("Arkanoid Game");
        primaryStage.show();

        SoundManager.playBackground("background.mp3", true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
