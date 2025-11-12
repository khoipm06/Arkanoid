package com.arkanoid.ui.view;

import com.arkanoid.database.entity.User;
import com.arkanoid.database.UserManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.components.ToastNotification;
import org.slf4j.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class SignIn {
    private static final Logger logger = GameLogger.getLogger(SignIn.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private AnchorPane rootSignIn;
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
        soundManager.playSound("Accept.wav");

        String username = userNameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            String message = "Please enter all required information!";
            logger.warn(message);
            ToastNotification.showToast(message, rootSignIn, ToastNotification.ToastType.WARNING);
            return;
        }

        // Query database for user authentication
        User user = UserManager.login(username, password);

        if (user == null) {
            String message = "Login failed - incorrect username or password";
            logger.warn(message);
            ToastNotification.showToast(message, rootSignIn, ToastNotification.ToastType.ERROR);
            userNameField.clear();
            passwordField.clear();
            return;
        }

        // Log in through SessionManager (stores user ID for database queries)
        logger.info("Login successful!");
        logger.info("Hello {} (ID: {})", username, user.getId());
        SessionManager.login(user.getId(), user.getUsername(), user.getPasswordHash());

        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    public void onCancelClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");

        SessionManager.logout();
        userNameField.clear();
        passwordField.clear();

        SceneManager.switchTo("authScreen");
    }

    @FXML
    public void onSignUpLinkClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("signUpView");
    }

}
