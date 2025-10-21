package com.arkanoid;

import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.view.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApplication extends Application {
    public static final double GAME_WIDTH = 800;
    public static final double GAME_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws IOException {
//        primaryStage.setTitle("Arkanoid Game");
//        primaryStage.setResizable(false);
//
//        MainMenuScene mainMenu = new MainMenuScene(primaryStage, GAME_WIDTH, GAME_HEIGHT);
//        primaryStage.setScene(mainMenu.getScene());
//
//        primaryStage.show();
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

        SceneManager.switchTo("mainMenuView");
        primaryStage.setTitle("Arkanoid Game");
        primaryStage.show();

        SoundManager.playBackground("background.mp3", true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
