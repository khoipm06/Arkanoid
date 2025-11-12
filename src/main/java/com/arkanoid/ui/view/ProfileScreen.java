package com.arkanoid.ui.view;

import com.arkanoid.database.InventoryManager;
import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.systems.sound.SoundManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class ProfileScreen {
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private AnchorPane authPane;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label userIdLabel;

    @FXML
    private Label balance;

    @FXML
    private Label highScoreLabel;

    @FXML
    private Label gamesPlayedLabel;

    @FXML
    private Label totalScoreLabel;

    @FXML
    private Label currentSkinLabel;

    @FXML
    private Label inventoryLabel;

    @FXML
    private Label thongtin;

    @FXML
    public void initialize() {
        refreshProfile();
    }

    public void refreshProfile() {
        SessionManager.User user = SessionManager.getCurrentUser();
        if (user != null) {
            // Get fresh user data from database
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(user.getId());
            List<InventoryManager.InventoryItem> inventory = InventoryManager.getUserInventory(user.getId());

            // Display user info
            usernameLabel.setText("Username: " + user.getUsername());
            userIdLabel.setText("User ID: " + user.getId());
            balance.setText("Balance: $" + String.format("%,d", user.getMoney()));

            // Display game statistics
            if (profile != null) {
                highScoreLabel.setText("🏆 High Score: " + profile.highScore);
                gamesPlayedLabel.setText("🎮 Games Played: " + profile.gamesPlayed);
                totalScoreLabel.setText("📊 Total Score: " + profile.totalScore);
                currentSkinLabel.setText("🎨 Equipped Skin: " + profile.currentSkin);
            } else {
                highScoreLabel.setText("🏆 High Score: 0");
                gamesPlayedLabel.setText("🎮 Games Played: 0");
                totalScoreLabel.setText("📊 Total Score: 0");
                currentSkinLabel.setText("🎨 Equipped Skin: default");
            }

            // Display inventory
            if (inventory.isEmpty()) {
                inventoryLabel.setText("🛒 No items yet. Visit the shop to get started!");
            } else {
                StringBuilder invText = new StringBuilder();
                for (InventoryManager.InventoryItem item : inventory) {
                    String emoji = "📦";
                    String itemName = item.getItemId();
                    
                    if (itemName.startsWith("skin:")) {
                        emoji = "⚽";
                        itemName = itemName.substring(5);
                    } else if (itemName.startsWith("paddle:")) {
                        emoji = "🎯";
                        itemName = itemName.substring(7);
                        if (itemName.startsWith("equipped:")) {
                            continue; // Skip equipped markers
                        }
                    }
                    
                    invText.append(emoji).append(" ").append(itemName);
                    if (item.getQuantity() > 1) {
                        invText.append(" (x").append(item.getQuantity()).append(")");
                    }
                    invText.append("\n");
                }
                inventoryLabel.setText(invText.toString().trim());
            }
        } else {
            usernameLabel.setText("Username: Guest");
            userIdLabel.setText("User ID: N/A");
            balance.setText("Balance: $0");
            highScoreLabel.setText("🏆 High Score: 0");
            gamesPlayedLabel.setText("🎮 Games Played: 0");
            totalScoreLabel.setText("📊 Total Score: 0");
            currentSkinLabel.setText("🎨 Equipped Skin: default");
            inventoryLabel.setText("Please log in to view your profile.");
        }
    }

    @FXML
    void onBackClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }

    @FXML
    void onLogOutClick(MouseEvent event) {
        SessionManager.logout();
        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("mainMenuView");
    }
}
