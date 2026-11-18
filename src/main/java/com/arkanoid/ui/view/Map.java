package com.arkanoid.ui.view;

import com.arkanoid.core.GameConstants;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import com.arkanoid.utils.ImageLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Map {
    private static final Logger logger = GameLogger.getLogger(Map.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Button nextMapButton;
    @FXML
    private Button preMapButton;
    @FXML
    private ImageView mapView;
    @FXML
    private Button playGameButton;
    @FXML
    private Button backToMainMenuButton;

    private Stage stage;
    private int currentMapIndex = 0;
    private List<String> mapImages;
    private List<String> mapFiles;
    private static int unlockedLevel = GameConstants.MAX_LEVEL;

    public static void setUnlockedLevel(int level) {
        if (level > unlockedLevel) {
            unlockedLevel = level;
            logger.info("Unlocked map {}", unlockedLevel);
        }
    }

    public static int getUnlockedLevel() {
        return unlockedLevel;
    }

    public void initialize() {
        mapImages = new ArrayList<>();
        mapFiles = new ArrayList<>();

        mapImages.add("/images/map1_preview.png");
        mapImages.add("/images/map2_preview.png");
        mapImages.add("/images/map3_preview.png");

        mapFiles.add("/levels/level1.json");
        mapFiles.add("/levels/level2.json");
        mapFiles.add("/levels/level3.json");

        updatePreview();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onNextMapButtonClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        if (currentMapIndex < mapImages.size() - 1) {
            currentMapIndex++;
            updatePreview();
        } else {
            logger.debug("Already at the last map");
        }
    }

    @FXML
    public void onPreMapButtonClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        if (currentMapIndex > 0) {
            currentMapIndex--;
            updatePreview();
        } else {
            logger.debug("Already at the first map");
        }
    }

    @FXML
    private void onPlayGameButtonClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        int selectedLevel = currentMapIndex + 1;
        GameScene gameScene = GameScene.getInstance(
                stage != null ? stage : SceneManager.getStage(),
                GameConstants.WINDOW_WIDTH,
                GameConstants.WINDOW_HEIGHT,
                selectedLevel
        );
        gameScene.start();
    }

    @FXML
    private void onBackToMainMenuClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }

    private void updatePreview() {
        String imagePath = mapImages.get(currentMapIndex);
        Image img = ImageLoader.loadImage(imagePath);
        if (img != null) {
            mapView.setImage(img);
        } else {
            logger.error("Map preview failed: Unable to load image resource at path: '{}'", imagePath);
        }

        if (currentMapIndex + 1 > unlockedLevel) {
            mapView.setOpacity(0.4);
            playGameButton.setDisable(true);
        } else {
            mapView.setOpacity(1.0);
            playGameButton.setDisable(false);
        }
        preMapButton.setVisible(currentMapIndex > 0);
        nextMapButton.setVisible(currentMapIndex < mapImages.size() - 1);
    }
}
