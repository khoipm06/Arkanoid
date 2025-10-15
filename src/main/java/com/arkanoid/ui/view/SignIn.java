package com.arkanoid.ui.view;

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
        String username = userNameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!SessionManager.login(username, password)) {
            System.out.println("Đăng nhập không thành công");
            userNameField.clear();
            passwordField.clear();
            return;
        }
        System.out.println("Đăng nhập thành công!");
        System.out.println(" Hello username: " + username);

        SceneManager.switchTo("mainMenuView");
    }
    @FXML
    public void onCancelClick(MouseEvent event) {
        SessionManager.logout();
        userNameField.clear();
        passwordField.clear();

        SceneManager.switchTo("authScreen");
    }
    @FXML
    public void onSignUpLinkClick(MouseEvent event) {
        SceneManager.switchTo("signUpView");
    }

}
