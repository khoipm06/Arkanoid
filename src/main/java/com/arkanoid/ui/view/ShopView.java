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

    @FXML
    private IntegerProperty money = new SimpleIntegerProperty(1000);

    @FXML
    public void initialize() {
        lblMoney.textProperty().bind(money.asString("Balance : %d"));
    }

    private void showMessage(String text) {
        lblMessage.setText(text);
        lblMessage.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> lblMessage.setVisible(false));
        pause.play();
    }
    private void buyItem() {
        int itemPrice = 200;
        if (money.get() >= itemPrice) {
            money.set(money.get() - itemPrice);
            lblMessage.setText("Item purchased successfully!");
        } else {
            lblMessage.setText("Not enough money!");
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
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Enter the amount you want to deposit:");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                int amount = Integer.parseInt(amountStr);
                if (amount > 0) {
                    money.set(money.get() + amount);
                    showMessage("Deposit successful! +" + amount);
                } else {
                    showMessage("Deposit amount must be greater than 0!");
                }
            } catch (NumberFormatException e) {
                showMessage("Please enter a valid number!");
            }
        });
    }
}
