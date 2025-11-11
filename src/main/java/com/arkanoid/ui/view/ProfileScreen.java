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
    private Label balance;

    @FXML
    private Label thongtin;


    @FXML
    public void initialize() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
            usernameLabel.setText("Username :   " + user.getUsername());
            balance.setText(String.valueOf("Balance :   " + user.getMoney())); // giả sử User có thuộc tính balance
        } else {
            usernameLabel.setText("Username : Guest");
            balance.setText("Balance : 0");
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
