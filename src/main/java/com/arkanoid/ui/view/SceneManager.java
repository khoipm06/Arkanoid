package com.arkanoid.ui.view;

import com.arkanoid.systems.logging.GameLogger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static final Logger logger = GameLogger.getLogger(SceneManager.class);
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
            // Refresh data for scenes that need it
            refreshSceneIfNeeded(name);
            mainStage.setScene(scenes.get(name));
            logger.info("Scene switched to: {}", name);
        } else {
            logger.error("Failed to switch scene: '{}' not registered in SceneManager or stage is null", name);
        }
    }

    /**
     * Refresh data for scenes that display dynamic content
     */
    private static void refreshSceneIfNeeded(String sceneName) {
        Object controller = controllers.get(sceneName);
        if (controller == null) return;

        // Refresh profile screen
        if ("profileScreen".equals(sceneName) && controller instanceof ProfileScreen profileController) {
            profileController.refreshProfile();
        }
        // Refresh leaderboard
        else if ("leaderboard".equals(sceneName) && controller instanceof LeaderboardController leaderboardController) {
            leaderboardController.refreshData();
        }
    }

    public static Object getController(String name) {
        return controllers.get(name);
    }

    public static void showGameOver(int level, int score, String timePlayed) {
        Object controller = getController("gameOver");
        if (controller instanceof GameOver gameOverController) {
            gameOverController.init(level, score, timePlayed);
        }
        SceneManager.switchTo("gameOver");
    }

    public static void showWinLevel(int level, int score, String timePlayed) {
        Object controller = getController("winLevel");
        if (controller instanceof WinLevel winLevelController) {
            winLevelController.init(level, score, timePlayed);
        }
        SceneManager.switchTo("winLevel");
    }

}
