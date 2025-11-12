package com.arkanoid.ui.view;

import com.arkanoid.database.UserManager;
import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class SignIn {
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
            System.out.println("⚠️ Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Query database for user authentication
        UserManager.User user = UserManager.login(username, password);

        if (user == null) {
            System.out.println("❌ Đăng nhập không thành công - sai tên hoặc mật khẩu");
            userNameField.clear();
            passwordField.clear();
            return;
        }

        // Log in through SessionManager (stores user ID for database queries)
        System.out.println("✅ Đăng nhập thành công!");
        System.out.println("👋 Hello " + username + " (ID: " + user.getId() + ")");
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
