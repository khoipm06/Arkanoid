package com.arkanoid.ui.view;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class AuthScreen {
    @FXML
    public void onSignInButtonClick() {
        SceneManager.switchTo("signIn");
        SessionManager.User user = new SessionManager.User("TenNguoiChoi");
        SessionManager.login(user); //

    }

    public void onBackClick(MouseEvent event) {
        SceneManager.switchTo("mainMenuView");
    }
    @FXML
    public void onSignUpButtonClick(MouseEvent event) {
        SceneManager.switchTo("signUpView");
        SessionManager.User user = new SessionManager.User("TenNguoiChoi");
        SessionManager.login(user); //
    }

}
