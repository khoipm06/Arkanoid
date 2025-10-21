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

    public void initialize() {
        mapImages = new ArrayList<>();
        mapFiles = new ArrayList<>();

        mapImages.add("/maps/map1_preview.png");
        mapImages.add("/maps/map2_preview.png");
        mapImages.add("/maps/map3_preview.png");

        mapFiles.add("/maps/map1.txt");
        mapFiles.add("/maps/map2.txt");
        mapFiles.add("/maps/map3.txt");

        updatePreview();
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void onNextMapButtonClick(MouseEvent event) {
        System.out.println("next map");
        SoundManager.playSound("Accept.wav");
        currentMapIndex = (currentMapIndex + 1) % mapImages.size();
        updatePreview();
    }
    @FXML
    public void onPreMapButtonClick(MouseEvent event) {
        System.out.println("pre map");
        SoundManager.playSound("Accept.wav");
        currentMapIndex = (currentMapIndex - 1 + mapImages.size()) % mapImages.size();
        updatePreview();
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
            System.err.println("LỖI: Không tìm thấy tệp ảnh tại đường dẫn: " + imagePath);
        }
    }
}
