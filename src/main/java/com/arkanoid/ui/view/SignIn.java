package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class SignIn {
    @FXML
    private Button signIn;
    @FXML
    private Button cancel;
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Hyperlink signUpClick;

    @FXML
    public void onSignInClick() {

        SoundManager.playSound("Accept.wav");

        String username = userNameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill in all fields!");
            return;
        }

        if (!SessionManager.login(username, password)) {
            System.out.println("Login failed");
            userNameField.clear();
            passwordField.clear();
            return;
        }
        System.out.println("Login successful!");
        System.out.println(" Hello username: " + username);

        SceneManager.switchTo("mainMenuView");
    }
    @FXML
    public void onCancelClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        SessionManager.logout();
        userNameField.clear();
        passwordField.clear();

        SceneManager.switchTo("authScreen");
    }
    @FXML
    public void onSignUpLinkClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("signUpView");
    }

}
