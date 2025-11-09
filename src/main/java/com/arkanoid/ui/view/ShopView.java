package com.arkanoid.ui.view;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.core.entities.Paddle;
import com.arkanoid.database.UserManager;
import com.arkanoid.systems.player.PlayerProfile;
import com.arkanoid.systems.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import java.util.Optional;

public class ShopView {
    @FXML
    private final IntegerProperty money = new SimpleIntegerProperty(1000);
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
        lblMessage.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 16px;" +
                        "-fx-effect: dropshadow( gaussian , rgba(0,0,0,0.7) , 5, 0.0 , 0 , 1 );" // Thêm bóng đổ đen để
                                                                                                 // chữ trắng nổi bật
                                                                                                 // hơn
        );

        lblMessage.setVisible(true);

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

        // Tạo dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nạp Tiền");
        dialog.setHeaderText("💰 Nhập số tiền bạn muốn nạp:");
        dialog.setContentText("Số tiền:");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, rgba(0, 51, 102, 0.95), rgba(0, 20, 60, 0.98));"
                        +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: linear-gradient(to bottom right, #00d4ff, #00bfff);" +
                        "-fx-border-width: 2.5;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 191, 255, 0.6), 20, 0.3, 0, 0);");

        dialogPane.setPrefSize(420, 260);

        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle(
                    "-fx-text-fill: #ffffff;" +
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-family: 'Segoe UI', 'Roboto', sans-serif;" +
                            "-fx-effect: dropshadow(gaussian, #00ffff, 8, 0.7, 0, 0);");
        }

        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle(
                    "-fx-text-fill: #e0f7ff;" +
                            "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-family: 'Segoe UI';");
        }

        TextField textField = dialog.getEditor();
        textField.setPromptText("VD: 50000");
        textField.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.15);" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #00bfff;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 12;" +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                textField.setStyle(textField.getStyle() +
                        "-fx-effect: dropshadow(gaussian, #00ffff, 12, 0.5, 0, 0);");
            } else {
                textField.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.15);" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
                                "-fx-font-size: 18px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-color: #00bfff;" +
                                "-fx-border-width: 1.5;" +
                                "-fx-border-radius: 12;" +
                                "-fx-padding: 12;" +
                                "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            }
        });

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setText("💸 Nạp Ngay");
        okButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #00d4ff, #0099cc);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);");

        okButton.setOnMouseEntered(e -> okButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #00f0ff, #00bfff);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 24;" +
                        "-fx-effect: dropshadow(gaussian, #00ffff, 15, 0.6, 0, 0);"));
        okButton.setOnMouseExited(e -> okButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #00d4ff, #0099cc);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 15px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);"));

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setText("❌ Hủy");
        cancelButton.setStyle(
                "-fx-background-color: #2d2d2d;" +
                        "-fx-text-fill: #ff6b6b;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 20;");

        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(
                "-fx-background-color: #3a3a3a;" +
                        "-fx-text-fill: #ff8787;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 20;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(
                "-fx-background-color: #2d2d2d;" +
                        "-fx-text-fill: #ff6b6b;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 20;"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                int amount = Integer.parseInt(amountStr.trim());
                if (amount > 0) {
                    SessionManager.User currentUser = SessionManager.getCurrentUser();
                    currentUser.addMoney(amount);
                    refreshMoney();

                    ShopBall shopBall = (ShopBall) SceneManager.getController("shopBall");
                    if (shopBall != null) {
                        shopBall.refreshMoney();
                    }
                    showMessage("🎉 Nạp thành công! +" + String.format("%,d", amount) + " xu");
                } else {
                    showMessage("Số tiền phải lớn hơn 0!");
                }
            } catch (NumberFormatException e) {
                showMessage("Vui lòng chỉ nhập số!");
            }
        });
    }
}
