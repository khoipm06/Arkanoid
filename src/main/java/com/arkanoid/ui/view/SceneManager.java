package com.arkanoid.ui.view;

import com.arkanoid.core.GameConstants;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.ui.GameScene;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages a single persistent {@code 1000 x 650} {@link Scene} on the primary {@link Stage},
 * unified viewport scaling, deterministic scene lifecycle transitions, and input handler cleanup.
 */
public class SceneManager {
    private static final Logger logger = GameLogger.getLogger(SceneManager.class);
    private static Stage mainStage;
    private static Scene mainScene;
    private static final Map<String, Parent> roots = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();
    private static final Map<String, SceneLifecycle> lifecycles = new HashMap<>();

    private static String activeSceneName;
    private static SceneLifecycle activeLifecycle;
    private static GameScene activeGameScene;

    /**
     * Attaches the primary {@link Stage} and initializes the single persistent {@link Scene}.
     */
    public static void setStage(Stage stage) {
        mainStage = stage;
        if (mainStage != null) {
            if (mainScene == null) {
                mainScene = new Scene(new StackPane(), GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
            }
            if (mainStage.getScene() != mainScene) {
                mainStage.setScene(mainScene);
            }
        }
    }

    public static Stage getStage() {
        return mainStage;
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    /**
     * Loads an FXML view, wraps it in {@link ViewportScaler} using its intrinsic design size,
     * and caches its root and controller without creating a separate {@link Scene}.
     */
    public static void loadScene(String name, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/arkanoid/ui/view/" + fxmlPath));
        Parent loadedRoot = loader.load();

        double designW = GameConstants.DEFAULT_DESIGN_WIDTH;
        double designH = GameConstants.DEFAULT_DESIGN_HEIGHT;
        if (loadedRoot instanceof Region region) {
            if (region.getPrefWidth() > 0.0) {
                designW = region.getPrefWidth();
            }
            if (region.getPrefHeight() > 0.0) {
                designH = region.getPrefHeight();
            }
        }

        Parent scaledRoot = ViewportScaler.wrap(loadedRoot, designW, designH);
        roots.put(name, scaledRoot);

        Object controller = loader.getController();
        controllers.put(name, controller);
        if (controller instanceof SceneLifecycle lifecycle) {
            lifecycles.put(name, lifecycle);
        } else {
            lifecycles.remove(name);
        }
    }

    /**
     * Switches the persistent {@link Scene} root to a pre-loaded FXML view.
     */
    public static void switchTo(String name) {
        if (mainStage == null || mainScene == null || !roots.containsKey(name)) {
            logger.error("Failed to switch scene: '{}' not registered in SceneManager or stage is null", name);
            return;
        }

        clearKeyHandlers();
        exitCurrentScene();

        Parent targetRoot = roots.get(name);
        activeSceneName = name;
        activeLifecycle = lifecycles.get(name);

        mainScene.setRoot(targetRoot);
        refreshSceneIfNeeded(name);
        if (activeLifecycle != null) {
            activeLifecycle.onEnter();
        }
        logger.info("Scene switched to: {}", name);
    }

    /**
     * Displays a dynamically constructed root (such as single-player or two-player gameplay)
     * inside the single persistent {@link Scene} with lifecycle tracking.
     */
    public static void showCustomRoot(String name, Parent root, SceneLifecycle lifecycle) {
        if (mainStage == null || mainScene == null || root == null) {
            logger.error("Failed to show custom root '{}': stage/scene/root is null", name);
            return;
        }

        clearKeyHandlers();
        exitCurrentScene();

        activeSceneName = name;
        activeLifecycle = lifecycle;
        if (lifecycle instanceof GameScene gameScene) {
            activeGameScene = gameScene;
        }

        mainScene.setRoot(root);
        if (activeLifecycle != null) {
            activeLifecycle.onEnter();
        }
        logger.info("Custom root displayed: {}", name);
    }

    /**
     * Registers key press and release handlers on the single persistent {@link Scene}.
     */
    public static void setKeyHandlers(EventHandler<? super KeyEvent> onPress,
                                      EventHandler<? super KeyEvent> onRelease) {
        if (mainScene != null) {
            mainScene.setOnKeyPressed(onPress);
            mainScene.setOnKeyReleased(onRelease);
        }
    }

    private static void clearKeyHandlers() {
        if (mainScene != null) {
            mainScene.setOnKeyPressed(null);
            mainScene.setOnKeyReleased(null);
        }
    }

    private static void exitCurrentScene() {
        SceneLifecycle previousLifecycle = activeLifecycle;
        GameScene previousGameScene = activeGameScene;

        activeLifecycle = null;
        activeGameScene = null;

        if (previousLifecycle != null) {
            try {
                previousLifecycle.onExit();
            } catch (Exception e) {
                logger.error("Error in SceneLifecycle.onExit for '{}': {}", activeSceneName, e.getMessage(), e);
            }
        }
        if (previousGameScene != null && previousGameScene != previousLifecycle) {
            try {
                previousGameScene.cleanup();
            } catch (Exception e) {
                logger.error("Error cleaning up active GameScene: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Track the active GameScene for cleanup when switching scenes.
     */
    public static void setActiveGameScene(GameScene gameScene) {
        activeGameScene = gameScene;
    }

    private static void refreshSceneIfNeeded(String sceneName) {
        Object controller = controllers.get(sceneName);
        if (controller == null) {
            return;
        }

        if ("profileScreen".equals(sceneName) && controller instanceof ProfileScreen profileController) {
            profileController.refreshProfile();
        } else if ("leaderboard".equals(sceneName) && controller instanceof LeaderboardController leaderboardController) {
            leaderboardController.refreshData();
        } else if ("shopView".equals(sceneName) && controller instanceof ShopView shopViewController) {
            shopViewController.refreshMoney();
        }
    }

    public static Object getController(String name) {
        return controllers.get(name);
    }

    public static String getActiveSceneName() {
        return activeSceneName;
    }

    public static void showGameOver(int level, int score, String timePlayed) {
        switchTo("gameOver");
        Object controller = getController("gameOver");
        if (controller instanceof GameOver gameOverController) {
            gameOverController.init(level, score, timePlayed);
        }
    }

    public static void showWinLevel(int level, int score, String timePlayed) {
        switchTo("winLevel");
        Object controller = getController("winLevel");
        if (controller instanceof WinLevel winLevelController) {
            winLevelController.init(level, score, timePlayed);
        }
    }

    /**
     * Releases active scene resources, clears listeners, and resets cached views on application exit.
     */
    public static void cleanup() {
        clearKeyHandlers();
        exitCurrentScene();
        roots.clear();
        controllers.clear();
        lifecycles.clear();
        activeSceneName = null;
        mainScene = null;
        mainStage = null;
        logger.info("SceneManager cleaned up");
    }
}
