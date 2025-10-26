package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class AuthScreen {
    @FXML
    public void onSignInButtonClick() {
        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("signIn");
        SessionManager.User user = new SessionManager.User("PlayerName");
        SessionManager.login(user);

    }

    public void onBackClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }
    @FXML
    public void onSignUpButtonClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("signUpView");
        SessionManager.User user = new SessionManager.User("PlayerName");
        SessionManager.login(user);
    }

}
