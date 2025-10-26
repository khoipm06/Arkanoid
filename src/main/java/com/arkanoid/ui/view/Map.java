package com.arkanoid.ui.view;

import com.arkanoid.GameApplication;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.GameScene;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;

public class Map {
    @FXML
    private Button nextMapButton;
    @FXML
    private Button preMapButton;
    @FXML
    private ImageView mapView;
    @FXML
    private Button playGameButton;

    private Stage stage;
    private int currentMapIndex = 0;
    private java.util.List<String> mapImages;
    private List<String> mapFiles;
    private static int unlockedLevel = 3;

    public static void setUnlockedLevel(int level) {
        if (level > unlockedLevel) {
            unlockedLevel = level;
            System.out.println("Unlocked map " + unlockedLevel);
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
        SoundManager.playSound("Accept.wav");
        if (currentMapIndex < mapImages.size() - 1) {
            currentMapIndex++;
            updatePreview();
        } else {
            System.out.println("Already at the last map!");
        }

    }

    @FXML
    public void onPreMapButtonClick(MouseEvent event) {
        System.out.println("pre map");
        SoundManager.playSound("Accept.wav");
        if (currentMapIndex > 0) {
            currentMapIndex--;
            updatePreview();
        } else {
            System.out.println("Already at the first map!");
        }
    }

    @FXML
    private void onPlayGameButtonClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");
        SoundManager.playSound("Accept.wav");

        if (stage == null) {
            stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        }
        int selectedLevel = currentMapIndex + 1;
        GameScene gameScene = new GameScene(stage, GameApplication.GAME_WIDTH, GameApplication.GAME_HEIGHT, selectedLevel);

        stage.setScene(gameScene.getScene());
        stage.show();
        gameScene.start();
    }


    private void updatePreview() {
        String imagePath = mapImages.get(currentMapIndex);
        java.io.InputStream is = getClass().getResourceAsStream(imagePath);

        if (is != null) {
            javafx.scene.image.Image img = new javafx.scene.image.Image(is);
            mapView.setImage(img);

            try {
                is.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("ERROR: Could not find image file at path: " + imagePath);
        }
        if (currentMapIndex + 1 > unlockedLevel) {
            mapView.setOpacity(0.4);
            playGameButton.setDisable(true);
        } else {
            mapView.setOpacity(1.0);
            playGameButton.setDisable(false);
        }
    }
}
