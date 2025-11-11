package com.arkanoid.ui.view;

import com.arkanoid.core.entities.Ball;
import com.arkanoid.database.UserManager;
import com.arkanoid.systems.player.PlayerProfile;
import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class ShopBall {
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Button buy1, buy2, buy3;
    @FXML
    private Button equip1, equip2, equip3;
    @FXML
    private Button back;
    @FXML
    private Label lblMoney;
    @FXML
    private ImageView imgFire, imgIce, imgRainbow;
    private Ball ballInGame;
    private final List<SkinItem> skinItems = new ArrayList<>();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) {
            UserManager.register("guest", "123", "");
            SessionManager.login(new SessionManager.User("guest"));
        }

        skinItems.add(new SkinItem("Fire", 1000, buy1, equip1, imgFire));
        skinItems.add(new SkinItem("Ice", 2000, buy2, equip2, imgIce));
        skinItems.add(new SkinItem("Rainbow", 3000, buy3, equip3, imgRainbow));

        for (SkinItem item : skinItems) {
            item.imageView.setImage(Ball.getSkin(item.name));
        }
    }

    public void setPlayer(PlayerProfile player, Ball ballInGame) {
        SessionManager.setActiveProfile(player);
        this.ballInGame = ballInGame;
        updateShopUI();
    }

    @FXML
    public void onBackClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        SceneManager.switchTo("shopView");
    }

    @FXML
    public void onBuy1Click(MouseEvent event) {
        buySkin("Fire");
    }

    @FXML
    public void onBuy2Click(MouseEvent event) {
        buySkin("Ice");
    }

    @FXML
    public void onBuy3Click(MouseEvent event) {
        buySkin("Rainbow");
    }

    @FXML
    public void onEquip1Click(MouseEvent event) {
        String selectedSkin = "Fire";
        SessionManager.setEquippedSkin(selectedSkin);
        equipSkin("Fire");
    }

    @FXML
    public void onEquip2Click(MouseEvent event) {
        String selectedSkin = "Ice";
        SessionManager.setEquippedSkin(selectedSkin);
        equipSkin("Ice");
    }

    @FXML
    public void onEquip3Click(MouseEvent event) {
        String selectedSkin = "Rainbow";
        SessionManager.setEquippedSkin(selectedSkin);
        equipSkin("Rainbow");
    }

    private void buySkin(String skinName) {
        SessionManager.User user = SessionManager.getCurrentUser();
        for (SkinItem item : skinItems) {
            if (item.name.equals(skinName)) {
                if (user.hasSkin(skinName)) {
                    System.out.println("Đã sở hữu skin: " + skinName);
                    return;
                }

                if (user.spendMoney(item.price)) {
                    user.addOwnedSkin(skinName);
                    System.out.println("Mua thành công skin: " + skinName);
                    lblMoney.setText("Tiền của bạn: " + user.getMoney() + "$");
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
        if (user.hasSkin(skinName)) {
            user.setEquippedSkin(skinName);
            SessionManager.setEquippedSkin(skinName);
            Ball.setCurrentSkin(skinName);
            System.out.println("Đã trang bị skin: " + skinName);
            if (ballInGame != null) {
                ballInGame.equipSkin(skinName);
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

        lblMoney.setText("Tiền của bạn: " + user.getMoney() + "$");

        for (SkinItem item : skinItems) {
            boolean owned = user.hasSkin(item.name);
            boolean equipped = user.getEquippedSkin().equals(item.name);
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
            lblMoney.setText("Tiền của bạn: " + user.getMoney() + "$");
        }
    }

    private void setButtonState(Button btn, String cssClass) {
        btn.getStyleClass().removeAll("buy-button", "owned-button", "equip-button", "equipped-button");
        btn.getStyleClass().add(cssClass);
    }

    private static class SkinItem {
        String name;
        int price;
        Button buyButton;
        Button equipButton;
        ImageView imageView;
        private SessionManager.User currentUser;

        SkinItem(String name, int price, Button buyButton, Button equipButton, ImageView imageView) {
            this.name = name;
            this.price = price;
            this.buyButton = buyButton;
            this.equipButton = equipButton;
            this.imageView = imageView;
        }

        public void setUser(SessionManager.User user) {
            this.currentUser = user;
            updateUI();
        }

        private void updateUI() {
            // Ví dụ: hiển thị tiền của người chơi trong shop
            if (currentUser != null) {
                System.out.println("Người chơi: " + currentUser.getUsername());
                System.out.println("Số tiền hiện tại: " + currentUser.getMoney());

            }
        }
    }
}
