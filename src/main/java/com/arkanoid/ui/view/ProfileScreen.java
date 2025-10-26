package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ProfileScreen {
    @FXML
    private AnchorPane authPane;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label highScoreLabel;

    @FXML
    public void initialize() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
        }
    }

    @FXML
    void onBackClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    void onLogOutClick(MouseEvent event) {
        SessionManager.logout();
        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }
}
