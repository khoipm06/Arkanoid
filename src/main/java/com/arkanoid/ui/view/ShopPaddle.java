package com.arkanoid.ui.view;

import com.arkanoid.core.entities.Paddle;
import com.arkanoid.database.UserManager;
import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class ShopPaddle {
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Button backHome;
    @FXML
    private Button buy1, buy2, buy3;
    @FXML
    private Button equip1, equip2, equip3;
    @FXML
    private ImageView image1, image2, image3;
    @FXML
    private Label lblMoney;
    private final List<SkinItem> skinItems = new ArrayList<>();
    private Paddle paddleInGame;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) {
            UserManager.register("guest", "123");
            SessionManager.login(new SessionManager.User("guest"));
        }

        skinItems.add(new SkinItem("paddle_Wood", 1000, buy1, equip1, image1));
        skinItems.add(new SkinItem("paddle_Metal", 2000, buy2, equip2, image2));
        skinItems.add(new SkinItem("paddle_Neon", 3000, buy3, equip3, image3));

        for (SkinItem item : skinItems) {
            item.imageView.setImage(Paddle.getSkin(item.name));
        }

        updateShopUI();
    }

    public void setPlayer(Paddle paddleInGame) {
        this.paddleInGame = paddleInGame;
        updateShopUI();
    }

    @FXML
    public void onBackHomeClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("shopView");
    }

    @FXML
    public void onBuy1Click(MouseEvent e) {
        buySkin("paddle_Wood");
    }

    @FXML
    public void onBuy2Click(MouseEvent e) {
        buySkin("paddle_Metal");
    }

    @FXML
    public void onBuy3Click(MouseEvent e) {
        buySkin("paddle_Neon");
    }

    @FXML
    public void onEquip1Click(MouseEvent e) {
        equipSkin("paddle_Wood");
    }

    @FXML
    public void onEquip2Click(MouseEvent e) {
        equipSkin("paddle_Metal");
    }

    @FXML
    public void onEquip3Click(MouseEvent e) {
        equipSkin("paddle_Neon");
    }

    private void buySkin(String skinName) {
        SessionManager.User user = SessionManager.getCurrentUser();
        for (SkinItem item : skinItems) {
            if (item.name.equals(skinName)) {
                if (user.hasPaddleSkin(skinName))
                    return; // đã sở hữu
                if (user.spendMoney(item.price)) {
                    user.addOwnedPaddleSkin(skinName);
                    lblMoney.setText("Số dư: " + user.getMoney() + "$");
                    ((ShopView) SceneManager.getController("shopView")).refreshMoney();
                } else {
                    System.out.println("Không đủ tiền để mua skin: " + skinName);
                }
                break;
            }
        }
        updateShopUI();
    }

    private void equipSkin(String skinName) {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user.hasPaddleSkin(skinName)) {
            user.setEquippedPaddleSkin(skinName);
            SessionManager.setEquippedPaddleSkin(skinName);
            Paddle.setCurrentSkin(skinName);
            System.out.println("Đã trang bị skin: " + skinName);
            if (paddleInGame != null) {
                paddleInGame.equipSkin(skinName);
            }
        } else {
            System.out.println("Chưa sở hữu skin: " + skinName);
        }
        updateShopUI();
    }

    private void updateShopUI() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user == null)
            return;

        lblMoney.setText("Số dư: " + user.getMoney() + "$");

        for (SkinItem item : skinItems) {
            boolean owned = user.hasPaddleSkin(item.name);
            boolean equipped = user.getEquippedPaddleSkin().equals(item.name);
            boolean canAfford = user.getMoney() >= item.price;

            item.buyButton.getStyleClass().removeAll("buy-button", "owned-button");
            item.equipButton.getStyleClass().removeAll("equip-button", "equipped-button");

            if (!owned) {
                item.buyButton.getStyleClass().add("buy-button"); // chưa mua
                item.buyButton.setDisable(!canAfford);
            } else {
                item.buyButton.getStyleClass().add("owned-button"); // đã mua
            }

            if (owned) {
                item.equipButton.setVisible(true);
                if (equipped) {
                    item.equipButton.getStyleClass().add("equipped-button"); // đang trang bị
                    item.equipButton.setDisable(true);
                } else {
                    item.equipButton.getStyleClass().add("equip-button"); // chưa trang bị
                    item.equipButton.setDisable(false);
                }
            } else {
                item.equipButton.setVisible(false);
            }
        }
    }

    public void refreshMoney() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
            lblMoney.setText("Số dư: " + user.getMoney() + "$");
        }
    }

    private static class SkinItem {
        String name;
        int price;
        Button buyButton;
        Button equipButton;
        ImageView imageView;

        SkinItem(String name, int price, Button buyButton, Button equipButton, ImageView imageView) {
            this.name = name;
            this.price = price;
            this.buyButton = buyButton;
            this.equipButton = equipButton;
            this.imageView = imageView;
        }
    }
}
