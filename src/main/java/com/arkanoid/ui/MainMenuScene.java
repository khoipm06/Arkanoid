package com.arkanoid.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenuScene {
    private Scene scene;
    private Stage stage;

    public MainMenuScene(Stage stage, double width, double height) {
        this.stage = stage;
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a2e;");

        Text title = new Text("ARKANOID");
        title.setFont(Font.font("Arial", 60));
        title.setFill(Color.CYAN);

        Button playButton = createButton("PLAY");
        Button settingsButton = createButton("SETTINGS");
        Button shopButton = createButton("SHOP");

        playButton.setOnAction(e -> {
            GameScene gameScene = new GameScene(stage, width, height);
            stage.setScene(gameScene.getScene());
            gameScene.start();
        });

        settingsButton.setOnAction(e -> {
        });

        shopButton.setOnAction(e -> {
        });

        root.getChildren().addAll(title, playButton, settingsButton, shopButton);
        
        this.scene = new Scene(root, width, height);
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", 24));
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setStyle(
            "-fx-background-color: #16213e;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: cyan;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;"
        );
        
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: #0f4c75;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: cyan;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 5px;" +
                "-fx-background-radius: 5px;"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: #16213e;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: cyan;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 5px;" +
                "-fx-background-radius: 5px;"
            )
        );
        
        return button;
    }

    public Scene getScene() {
        return scene;
    }
}
