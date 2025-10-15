package com.arkanoid.ui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ProfileScreen {
    @FXML
    private AnchorPane authPane; // Đặt ID này trong FXML của bạn

    @FXML
    private Label usernameLabel; // Thêm Label cho username trong FXML của bạn

    @FXML
    private Label highScoreLabel; // Thêm Label cho high score

    // Phương thức được gọi khi Controller được load (optional)
    @FXML
    public void initialize() {
        // Hiển thị thông tin người dùng khi màn hình được tải
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
            // Giả sử bạn đã thêm fx:id="usernameLabel" cho Label hiển thị username
            // usernameLabel.setText(user.getUsername());
            // highScoreLabel.setText(String.valueOf(user.getHighScore()));
        }
    }

    // Xử lý nút "back"
    @FXML
    void onBackClick(MouseEvent event) {
        // Ví dụ: Quay lại màn hình chính Home
        SceneManager.switchTo("mainMenuView");
    }

    // Xử lý nút "Log out"
    @FXML
    void onLogOutClick(MouseEvent event) {
        SessionManager.logout();

        // Chuyển về màn hình Auth
        SceneManager.switchTo("mainMenuView");
    }
}
