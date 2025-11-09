package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ModeSelectView {
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Button singlePlayer;
    @FXML
    private Button multiPlayer;
    @FXML
    private Button back;
    @FXML
    private AnchorPane root;

    private MainMenuView mainController;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onSinglePlayerClick(MouseEvent event) throws IOException {
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("map");
    }

    @FXML
    public void onMultiPlayerClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        Stage mainStage = SceneManager.getStage();
        if (mainStage != null) {
            TwoPlayerGameScreen twoPlayerScreen = new TwoPlayerGameScreen(mainStage);
            twoPlayerScreen.show();
        } else {
            System.err.println("Error: Main stage not found");
        }
    }

    @FXML
    public void onBackClick(MouseEvent event) {
        System.out.println("Quay lại menu chính");

        soundManager.playSound("Accept.wav");

        if (mainController != null) {
            mainController.closeModePopup();
        }

    }

    public void setMainController(MainMenuView controller) {
        this.mainController = controller;
    }

    public Button getSinglePlayerButton() {
        return singlePlayer;
    }

    public Button getMultiPlayerButton() {
        return multiPlayer;
    }

    public Button getBackButton() {
        return back;
    }
}
