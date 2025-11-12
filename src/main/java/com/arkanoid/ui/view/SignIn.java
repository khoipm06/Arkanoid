package com.arkanoid.ui.view;

import com.arkanoid.database.UserManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import org.slf4j.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class SignIn {
    private static final Logger logger = GameLogger.getLogger(SignIn.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

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
            logger.warn("Please enter all required information!");
            return;
        }

        // Query database for user authentication
        UserManager.User user = UserManager.login(username, password);

        if (user == null) {
            logger.warn("Login failed - incorrect username or password");
            userNameField.clear();
            passwordField.clear();
            return;
        }

        // Log in through SessionManager (stores user ID for database queries)
        logger.info("Login successful!");
        logger.info("Hello {} (ID: {})", username, user.getId());
        SessionManager.login(new SessionManager.User(user.getId(), user.getUsername()));

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
