package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class SignUpView {
    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button signUp;
    @FXML
    private Button cancel;
    @FXML
    private Hyperlink signUpClick;

    @FXML
    public void onSignUpClick() {

        SoundManager.playSound("Accept.wav");

        String username = userNameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            System.out.println("Please fill in all fields!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("Passwords do not match! Please re-enter.");
            return;
        }

        if (!SessionManager.register(username, password)) {
            System.out.println("Player name already exists!");
            return;
        }

        System.out.println("Registration successful!");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        SessionManager.User user = new SessionManager.User(username);
        SessionManager.login(user);

        SceneManager.switchTo("signIn");
    }

    @FXML
    public void onCancelClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        userNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        SceneManager.switchTo("signIn");
    }
    @FXML
    public void onSignInLinkClick(MouseEvent event) {
        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("signIn");
    }
}
