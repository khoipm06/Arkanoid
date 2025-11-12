package com.arkanoid.ui.view;

import com.arkanoid.database.InventoryManager;
import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.PlayerProfile;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages user session state
 * Now works with database for persistent user data
 */
public class SessionManager {
    private static final Logger logger = GameLogger.getLogger(SessionManager.class);
    private static Integer currentUserId = null;
    private static String currentUsername = null;
    private static PlayerProfile activeProfile;

    /**
     * User session wrapper that fetches live data from database
     */
    public static class User {
        private final int id;
        private final String username;

        public User(int id, String username) {
            this.id = id;
            this.username = username;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        /**
         * Get current coins from database
         */
        public int getMoney() {
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(id);
            return profile != null ? profile.coins : 0;
        }

        /**
         * Set money in database
         */
        public void setMoney(int money) {
            PlayerProfileManager.updateCoins(id, money);
        }

        /**
         * Add money to user's balance
         */
        public void addMoney(int amount) {
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(id);
            if (profile != null) {
                PlayerProfileManager.updateCoins(id, profile.coins + amount);
            }
        }

        /**
         * Spend money from user's balance
         */
        public boolean spendMoney(int amount) {
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(id);
            if (profile != null && profile.coins >= amount) {
                PlayerProfileManager.updateCoins(id, profile.coins - amount);
                return true;
            }
            return false;
        }

        /**
         * Add a skin to inventory
         */
        public void addOwnedSkin(String skin) {
            InventoryManager.addItem(id, "skin:" + skin, 1);
        }

        /**
         * Check if user owns a skin
         */
        public boolean hasSkin(String skin) {
            if ("Default".equals(skin)) {
                return true; // Everyone has default skin
            }
            return InventoryManager.hasItem(id, "skin:" + skin);
        }

        /**
         * Get equipped skin from database
         */
        public String getEquippedSkin() {
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(id);
            return profile != null ? profile.currentSkin : "Default";
        }

        /**
         * Set equipped skin in database
         */
        public void setEquippedSkin(String skin) {
            PlayerProfileManager.updateSkin(id, skin);
        }

        /**
         * Add a paddle skin to inventory
         */
        public void addOwnedPaddleSkin(String skin) {
            InventoryManager.addItem(id, "paddle:" + skin, 1);
        }

        /**
         * Check if user owns a paddle skin
         */
        public boolean hasPaddleSkin(String skin) {
            if ("paddle_Default".equals(skin)) {
                return true; // Everyone has default paddle
            }
            return InventoryManager.hasItem(id, "paddle:" + skin);
        }

        /**
         * Get equipped paddle skin (stored in inventory as current)
         */
        public String getEquippedPaddleSkin() {
            // For now, use a convention - could be stored in player_profiles
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(id);
            for (InventoryManager.InventoryItem item : items) {
                if (item.getItemId().startsWith("paddle:equipped:")) {
                    return item.getItemId().substring("paddle:equipped:".length());
                }
            }
            return "paddle_Default";
        }

        /**
         * Set equipped paddle skin
         */
        public void setEquippedPaddleSkin(String skin) {
            // Remove old equipped marker
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(id);
            InventoryManager.addItem(id, "paddle:equipped:" + skin, 1);
        }

        /**
         * Get all owned skins
         */
        public List<String> getOwnedSkins() {
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(id);
            return items.stream()
                    .filter(item -> item.getItemId().startsWith("skin:") && !item.getItemId().contains("equipped"))
                    .map(item -> item.getItemId().substring("skin:".length()))
                    .collect(Collectors.toList());
        }

        /**
         * Get all owned paddle skins
         */
        public List<String> getOwnedPaddleSkins() {
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(id);
            return items.stream()
                    .filter(item -> item.getItemId().startsWith("paddle:") && !item.getItemId().contains("equipped"))
                    .map(item -> item.getItemId().substring("paddle:".length()))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Check if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUserId != null;
    }

    /**
     * Log in a user (stores user ID for database queries)
     */
    public static void login(User user) {
        currentUserId = user.getId();
        currentUsername = user.getUsername();
    }

    /**
     * Log out the current user
     */
    public static void logout() {
        currentUserId = null;
        currentUsername = null;
        activeProfile = null;
    }

    /**
     * Get the current logged-in user
     * Returns a User wrapper that fetches data from database
     */
    public static User getCurrentUser() {
        if (currentUserId != null && currentUsername != null) {
            return new User(currentUserId, currentUsername);
        }
        return null;
    }

    /**
     * Set equipped skin for current user
     */
    public static void setEquippedSkin(String skin) {
        if (currentUserId != null) {
            PlayerProfileManager.updateSkin(currentUserId, skin);
        }
    }

    /**
     * Get equipped skin for current user
     */
    public static String getEquippedSkin() {
        if (currentUserId != null) {
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(currentUserId);
            if (profile != null) {
                return profile.currentSkin;
            }
        }
        return "Default";
    }

    /**
     * Set equipped paddle skin for current user
     */
    public static void setEquippedPaddleSkin(String skin) {
        if (currentUserId != null) {
            // Store as equipped marker in inventory
            InventoryManager.addItem(currentUserId, "paddle:equipped:" + skin, 1);
        }
    }

    /**
     * Get equipped paddle skin for current user
     */
    public static String getEquippedPaddleSkin() {
        if (currentUserId != null) {
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(currentUserId);
            for (InventoryManager.InventoryItem item : items) {
                if (item.getItemId().startsWith("paddle:equipped:")) {
                    return item.getItemId().substring("paddle:equipped:".length());
                }
            }
        }
        return "paddle_Default";
    }

    public static PlayerProfile getActiveProfile() {
        if (activeProfile == null) {
            activeProfile = new PlayerProfile("player1");
        }
        return activeProfile;
    }

    public static void setActiveProfile(PlayerProfile profile) {
        activeProfile = profile;
    }

    /**
     * Save player data (for compatibility, now happens automatically via database)
     */
    public static void savePlayer(PlayerProfile player) {
        if (currentUserId != null) {
            User user = getCurrentUser();
            if (user != null) {
                logger.info("Saved user: {} | Money: {} | Equipped skin: {}",
                        user.getUsername(), user.getMoney(), user.getEquippedSkin());
            }
        }
    }
}
