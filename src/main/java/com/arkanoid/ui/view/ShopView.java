package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.Optional;

public class ShopView {
    @FXML
    private Label lblMoney;

    @FXML
    private Button btnBuyItem;

    @FXML
    private Label lblMessage;
    @FXML
    private Button deposit;

    // Số tiền người chơi
    @FXML
    private IntegerProperty money = new SimpleIntegerProperty(1000);

    @FXML
    public void initialize() {
        lblMoney.textProperty().bind(money.asString("Số dư : %d"));
    }

    private void showMessage(String text) {
        lblMessage.setText(text);
        lblMessage.setVisible(true);

        // Dừng 2 giây rồi ẩn lại
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> lblMessage.setVisible(false));
        pause.play();
    }
    private void buyItem() {
        int itemPrice = 200;
        if (money.get() >= itemPrice) {
            money.set(money.get() - itemPrice);
            lblMessage.setText("Mua đồ thành công!");
        } else {
            lblMessage.setText("Không đủ tiền!");
        }
    }

    @FXML
    public void onBallShopClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        System.out.println("Shop ball");
    }
    @FXML
    public void onPaddleShopClick(MouseEvent event) {
        SoundManager.playSound("Accept.wav");

        System.out.println("Paddle Shop");
    }

    @FXML
    public void onBackClick(MouseEvent event) {
        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }

    public void onDepositClick(MouseEvent event) {
        SoundManager.playSound("Accept.wav");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp tiền");
        dialog.setHeaderText("Nhập số tiền bạn muốn nạp:");
        dialog.setContentText("Số tiền:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                int amount = Integer.parseInt(amountStr);
                if (amount > 0) {
                    money.set(money.get() + amount);
                    showMessage("Nạp tiền thành công! +" + amount);
                } else {
                    showMessage("Số tiền nạp phải lớn hơn 0!");
                }
            } catch (NumberFormatException e) {
                showMessage("Vui lòng nhập số hợp lệ!");
            }
        });
    }
}
