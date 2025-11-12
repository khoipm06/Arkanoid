package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

public class AuthScreen {
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    public void onSignInButtonClick() {
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("signIn");
    }

    public void onBackClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    public void onSignUpButtonClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("signUpView");
    }

}
