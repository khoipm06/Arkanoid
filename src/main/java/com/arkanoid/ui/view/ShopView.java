package com.arkanoid.ui.view;

import com.arkanoid.database.UserManager;
import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Paddle;
import com.arkanoid.systems.player.PlayerProfile;
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

    private SessionManager.User currentUser;
    @FXML
    private IntegerProperty money = new SimpleIntegerProperty(1000);

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            UserManager.register("guest", "123", "");
            SessionManager.login(new SessionManager.User("guest"));
            currentUser = SessionManager.getCurrentUser();
        }

        updateMoneyLabel();
    }

    public void refreshMoney() {
        currentUser = SessionManager.getCurrentUser();
        updateMoneyLabel();
    }

    private void updateMoneyLabel() {
        if (currentUser != null)
            lblMoney.setText("Số dư: " + currentUser.getMoney() + "$");
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

        PlayerProfile player = SessionManager.getActiveProfile();
        PlayerProfile.setCurrentPlayer(player);

        player.setCurrentSkin(SessionManager.getEquippedSkin());

        Ball ball = new Ball(0, 0, 10, 100);
        ball.equipSkin(player.getCurrentSkin());

        ShopBall shopBallController = (ShopBall) SceneManager.getController("shopBall");
        if (shopBallController != null) {
            shopBallController.setPlayer(player, ball);
        } else {
            System.out.println("Không tìm thấy controller shopBall!");
        }

        SceneManager.switchTo("shopBall");
    }

    @FXML
    public void onPaddleShopClick(MouseEvent event) {
        SoundManager.playSound("Accept.wav");

        PlayerProfile player = SessionManager.getActiveProfile();
        PlayerProfile.setCurrentPlayer(player);

        Paddle paddle = new Paddle(0, 0, 120, 20);
        paddle.equipSkin(player.getEquippedPaddleSkin());

        ShopPaddle shopPaddleController = (ShopPaddle) SceneManager.getController("shopPaddle");
        if (shopPaddleController != null) {
            shopPaddleController.setPlayer(paddle);
        } else {
            System.out.println("Không tìm thấy controller shopPaddle!");
        }

        SceneManager.switchTo("shopPaddle");
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
                    SessionManager.User currentUser = SessionManager.getCurrentUser();
                    currentUser.addMoney(amount);
                    refreshMoney();

                    ShopBall shopBall = (ShopBall) SceneManager.getController("shopBall");
                    if (shopBall != null) {
                        shopBall.refreshMoney();
                    }
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
