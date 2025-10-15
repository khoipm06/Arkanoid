package com.arkanoid.ui.view;

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
        String username = userNameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            System.out.println("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("Mật khẩu không khớp! Vui lòng nhập lại.");
            return;
        }

        if (!SessionManager.register(username, password)) {
            System.out.println("Tên người chơi đã tồn tại!");
            return;
        }

        System.out.println("Đăng ký thành công!");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        SessionManager.User user = new SessionManager.User(username);
        SessionManager.login(user);

        SceneManager.switchTo("signIn");
    }

    @FXML
    public void onCancelClick(MouseEvent event) {
        userNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        SceneManager.switchTo("signIn");
    }
    @FXML
    public void onSignInLinkClick(MouseEvent event) {
        SceneManager.switchTo("signIn");
    }
}
