package com.arkanoid.ui.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static Stage mainStage;
    private static final Map<String, Scene> scenes = new HashMap<>();

    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    public static void loadScene(String name, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com.arkanoid.ui.view/" + fxmlPath));
        Scene scene = new Scene(loader.load());
        scenes.put(name, scene);
    }

    public static void switchTo(String name) {
        if (mainStage != null && scenes.containsKey(name)) {
            mainStage.setScene(scenes.get(name));
        } else {
            System.out.println("Scene not found: " + name);
        }
    }
}
