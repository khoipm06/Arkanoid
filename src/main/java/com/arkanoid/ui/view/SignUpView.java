package com.arkanoid.ui.view;

import com.arkanoid.database.UserManager;
import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class SignUpView {
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
            System.out.println("⚠️ Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("❌ Mật khẩu không khớp! Vui lòng nhập lại.");
            return;
        }

        // Register user in database (creates user + default profile)
        UserManager.User newUser = UserManager.register(username, password);

        if (newUser == null) {
            System.out.println("❌ Tên người chơi đã tồn tại!");
            return;
        }

        System.out.println("✅ Đăng ký thành công!");
        System.out.println("👤 Username: " + username);
        System.out.println("🆔 User ID: " + newUser.getId());
        System.out.println("📅 Created: " + newUser.getCreatedAt());

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
