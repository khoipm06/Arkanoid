package com.arkanoid.ui.view;

import com.arkanoid.database.UserManager;
import com.arkanoid.database.entity.User;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.components.ToastNotification;
import org.slf4j.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class SignUpView {
    private static final Logger logger = GameLogger.getLogger(SignUpView.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private AnchorPane rootSignUp;
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
        soundManager.playSound("Accept.wav");

        String username = userNameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            String message = "Please enter all required information!";
            logger.warn(message);
            ToastNotification.showToast(message, rootSignUp, ToastNotification.ToastType.WARNING);
            return;
        }

        if (!password.equals(confirmPassword)) {
            String message = "Passwords do not match! Please try again.";
            logger.warn(message);
            ToastNotification.showToast(message, rootSignUp, ToastNotification.ToastType.WARNING);
            return;
        }

        // Register user in database (creates user + default profile)
        User newUser = UserManager.register(username, password);

        if (newUser == null) {
            String message = "Username already exists!";
            logger.warn(message);
            ToastNotification.showToast(message, rootSignUp, ToastNotification.ToastType.ERROR);
            return;
        }

        logger.info("Registration successful!");
        logger.info("Auto logging in after registration...");
        logger.info("Username: {}", username);
        logger.info("User ID: {}", newUser.getId());
        logger.info("Created: {}", newUser.getCreatedAt());

        SessionManager.login(newUser.getId(), newUser.getUsername(), newUser.getPasswordHash());
        ToastNotification.showToast("Registration successful! Welcome!", rootSignUp, ToastNotification.ToastType.SUCCESS);
        SceneManager.switchTo("signIn");
    }

    @FXML
    public void onCancelClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");

        userNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        SceneManager.switchTo("authScreen");
    }

    @FXML
    public void onSignInLinkClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("signIn");
    }
}
