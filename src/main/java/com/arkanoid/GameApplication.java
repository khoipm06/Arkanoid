package com.arkanoid;

import com.arkanoid.ui.MainMenuScene;
import javafx.application.Application;
import javafx.stage.Stage;

public class GameApplication extends Application {
    private static final double GAME_WIDTH = 800;
    private static final double GAME_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Arkanoid Game");
        primaryStage.setResizable(false);

        MainMenuScene mainMenu = new MainMenuScene(primaryStage, GAME_WIDTH, GAME_HEIGHT);
        primaryStage.setScene(mainMenu.getScene());
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
