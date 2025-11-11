package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class SettingView {
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
            SoundManager.setVolume(vol / 100.0);
        });
    }
    @FXML
    private void onOKClick() {
        savedVolume = (int) volumeSlider.getValue();
        System.out.println("Volume saved: " + savedVolume + "%");

        SoundManager.playSound("Accept.wav");
        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    private void onCancelClick() {
        volumeSlider.setValue(savedVolume); // quay lại giá trị cũ
        System.out.println("Volume reverted to: " + savedVolume + "%");

        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }
}
