package com.arkanoid.ui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ModeSelectView {
    @FXML
    private Button singlePlayer;
    @FXML
    private Button multiPlayer;
    @FXML
    private Button back;
    @FXML
    private AnchorPane root;

    private MainMenuView mainController;

    @FXML
    public void onSinglePlayerClick(MouseEvent event) {
        System.out.println("Single Player");
        SceneManager.switchTo("singlePlayerScene");
    }
    @FXML
    public void onMultiPlayerClick(MouseEvent event) {
        System.out.println("Multi Player");
        SceneManager.switchTo("multiPlayerScene");
    }
    @FXML
    public void onBackClick(MouseEvent event) {
        System.out.println("Quay lại menu chính");
        if (mainController != null) {
            mainController.closeModePopup();
        }
        
    }

    public void setMainController(MainMenuView controller) {
        this.mainController = controller;
    }
}
