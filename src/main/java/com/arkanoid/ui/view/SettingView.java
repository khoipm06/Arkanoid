package com.arkanoid.ui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class SettingView {
    @FXML
    private Slider volumeSlider;

    @FXML
    private Label volumeLabel;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    private int savedVolume = 50;

    @FXML
    public void initialize() {
        // Khi kéo slider, cập nhật label
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeLabel.setText(newVal.intValue() + "%");
        });
    }
    // OK: lưu âm lượng và quay lại màn hình chính
    @FXML
    private void onOKClick() {
        savedVolume = (int) volumeSlider.getValue();
        System.out.println("Volume saved: " + savedVolume + "%");

        // quay lại màn hình chính (đóng cửa sổ Setting)
        SceneManager.switchTo("mainMenuView");
    }

    // Cancel: hủy chỉnh, giữ nguyên màn hình Setting
    @FXML
    private void onCancelClick() {
        volumeSlider.setValue(savedVolume); // quay lại giá trị cũ
        System.out.println("Volume reverted to: " + savedVolume + "%");

        SceneManager.switchTo("mainMenuView");
        // không đóng cửa sổ, vẫn ở màn hình Setting
    }
}
