package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class SettingView {
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label valueLabel;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    private int savedVolume = 50;

    @FXML
    public void initialize() {
        // Khi kéo slider, cập nhật label
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int vol = newVal.intValue();
            valueLabel.setText(vol + "%");
            soundManager.setVolume(vol / 100.0);
        });
    }

    @FXML
    private void onOKClick() {
        savedVolume = (int) volumeSlider.getValue();
        System.out.println("Volume saved: " + savedVolume + "%");

        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    private void onCancelClick() {
        volumeSlider.setValue(savedVolume); // quay lại giá trị cũ
        System.out.println("Volume reverted to: " + savedVolume + "%");

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }
}
