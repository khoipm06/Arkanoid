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
    private static final Map<String, Object> controllers = new HashMap<>();

    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    public static Stage getStage() {
        return mainStage;
    }

    public static void loadScene(String name, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/arkanoid/ui/view/" + fxmlPath));
        Scene scene = new Scene(loader.load());
        scenes.put(name, scene);
        controllers.put(name, loader.getController());
    }

    public static void switchTo(String name) {
        if (mainStage != null && scenes.containsKey(name)) {
            mainStage.setScene(scenes.get(name));
        } else {
            System.out.println("Scene not found: " + name);
        }
    }

    public static Object getController(String name) {
        return controllers.get(name);
    }

    public static void showGameOver(int level, int score, String timePlayed) {
        Object controller = getController("gameOver");
        if (controller instanceof com.arkanoid.ui.view.GameOver gameOverController) {
            gameOverController.init(level, score, timePlayed);
        }
        SceneManager.switchTo("gameOver");
    }

    public static void showWinLevel(int level, int score, String timePlayed) {
        Object controller = getController("winLevel");
        if (controller instanceof com.arkanoid.ui.view.WinLevel winLevelController) {
            winLevelController.init(level, score, timePlayed);
        }
        SceneManager.switchTo("winLevel");
    }

}
