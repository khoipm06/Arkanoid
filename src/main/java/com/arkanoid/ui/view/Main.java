package com.arkanoid.ui.view;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        SceneManager.loadScene("mainMenuView", "MainMenuView.fxml");
        SceneManager.loadScene("modeSelectView", "ModeSelectView.fxml");
        SceneManager.loadScene("shopView", "ShopView.fxml");
        SceneManager.loadScene("settingView", "SettingView.fxml");
        SceneManager.loadScene("profileScreen", "ProfileScreen.fxml");
        SceneManager.loadScene("authScreen", "AuthScreen.fxml");
        SceneManager.loadScene("signIn", "SignIn.fxml");
        SceneManager.loadScene("signUpView", "SignUpView.fxml");

        SceneManager.switchTo("mainMenuView");
        stage.setTitle("Arkanoid Game");
        stage.show();
    }
    public static void switchToProfileOrAuth() {
        if (SessionManager.isLoggedIn()) {
            SceneManager.switchTo("profileScreen");
        } else {
            SceneManager.switchTo("authScreen");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
