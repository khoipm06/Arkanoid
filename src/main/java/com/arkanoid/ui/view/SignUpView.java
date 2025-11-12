package com.arkanoid.ui.view;

import com.arkanoid.database.UserManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import org.slf4j.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class SignUpView {
    private static final Logger logger = GameLogger.getLogger(SignUpView.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

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
            logger.warn("Please enter all required information!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            logger.warn("Passwords do not match! Please try again.");
            return;
        }

        // Register user in database (creates user + default profile)
        UserManager.User newUser = UserManager.register(username, password);

        if (newUser == null) {
            logger.warn("Username already exists!");
            return;
        }

        logger.info("Registration successful!");
        logger.info("Username: {}", username);
        logger.info("User ID: {}", newUser.getId());
        logger.info("Created: {}", newUser.getCreatedAt());

        SceneManager.switchTo("signIn");
    }

    @FXML
    public void onCancelClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");

        userNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        SceneManager.switchTo("signIn");
    }

    @FXML
    public void onSignInLinkClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("signIn");
    }
}
