package com.arkanoid.ui.view;

import com.arkanoid.database.InventoryManager;
import com.arkanoid.database.PlayerProfileManager;
import com.arkanoid.database.RepositoryFactory;
import com.arkanoid.database.UserSessionStorage;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.player.PlayerProfile;
import com.arkanoid.utils.PasswordHasher;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages user session state with write-through in-memory caching backed by SQLite.
 */
public class SessionManager {
    private static final Logger logger = GameLogger.getLogger(SessionManager.class);
    private static Integer currentUserId = null;
    private static String currentUsername = null;
    private static volatile User currentUser = null;
    private static PlayerProfile activeProfile;

    /**
     * User session wrapper with write-through caching for profile and inventory state.
     */
    public static class User {
        private final int id;
        private final String username;

        private boolean profileLoaded = false;
        private boolean profileExists = false;
        private int cachedMoney = 0;
        private String cachedEquippedSkin = "Default";

        private boolean inventoryLoaded = false;
        private Set<String> cachedOwnedSkins;
        private Set<String> cachedOwnedPaddleSkins;
        private Set<String> cachedOwnedBallSkins;
        private String cachedEquippedPaddleSkin = "paddle_Default";
        private String cachedEquippedBallSkin = "Default";

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

        private synchronized void ensureProfileLoaded() {
            if (profileLoaded) {
                return;
            }
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(id);
            profileLoaded = true;
            if (profile != null) {
                profileExists = true;
                cachedMoney = profile.money;
                cachedEquippedSkin = profile.currentSkin != null ? profile.currentSkin : "Default";
            } else {
                profileExists = false;
                cachedMoney = 0;
                cachedEquippedSkin = "Default";
            }
        }

        private synchronized void ensureInventoryLoaded() {
            if (inventoryLoaded) {
                return;
            }
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(id);
            cachedOwnedSkins = new LinkedHashSet<>();
            cachedOwnedPaddleSkins = new LinkedHashSet<>();
            cachedOwnedBallSkins = new LinkedHashSet<>();
            cachedEquippedPaddleSkin = "paddle_Default";
            cachedEquippedBallSkin = "Default";

            for (InventoryManager.InventoryItem item : items) {
                String itemId = item.getItemId();
                if (itemId.startsWith("skin:equipped:")) {
                    cachedEquippedBallSkin = itemId.substring("skin:equipped:".length());
                } else if (itemId.startsWith("skin:")) {
                    cachedOwnedSkins.add(itemId.substring("skin:".length()));
                } else if (itemId.startsWith("paddle:equipped:")) {
                    cachedEquippedPaddleSkin = itemId.substring("paddle:equipped:".length());
                } else if (itemId.startsWith("paddle:")) {
                    cachedOwnedPaddleSkins.add(itemId.substring("paddle:".length()));
                } else if (itemId.startsWith("ball:")) {
                    cachedOwnedBallSkins.add(itemId.substring("ball:".length()));
                }
            }
            inventoryLoaded = true;
        }

        public synchronized int getMoney() {
            ensureProfileLoaded();
            return cachedMoney;
        }

        public synchronized void setMoney(int money) {
            PlayerProfileManager.updateMoney(id, money);
            ensureProfileLoaded();
            cachedMoney = money;
        }

        public synchronized void addMoney(int amount) {
            ensureProfileLoaded();
            if (profileExists) {
                int updated = cachedMoney + amount;
                PlayerProfileManager.updateMoney(id, updated);
                cachedMoney = updated;
            }
        }

        public synchronized boolean spendMoney(int amount) {
            ensureProfileLoaded();
            if (profileExists && cachedMoney >= amount) {
                int updated = cachedMoney - amount;
                PlayerProfileManager.updateMoney(id, updated);
                cachedMoney = updated;
                return true;
            }
            return false;
        }

        public synchronized void addOwnedSkin(String skin) {
            InventoryManager.addItem(id, "skin:" + skin, 1);
            ensureInventoryLoaded();
            cachedOwnedSkins.add(skin);
        }

        public synchronized boolean hasSkin(String skin) {
            if ("Default".equals(skin)) {
                return true;
            }
            ensureInventoryLoaded();
            return cachedOwnedSkins.contains(skin);
        }

        public synchronized String getEquippedSkin() {
            ensureProfileLoaded();
            return cachedEquippedSkin;
        }

        public synchronized void setEquippedSkin(String skin) {
            PlayerProfileManager.updateSkin(id, skin);
            ensureProfileLoaded();
            cachedEquippedSkin = skin != null ? skin : "Default";
        }

        public synchronized void addOwnedPaddleSkin(String skin) {
            InventoryManager.addItem(id, "paddle:" + skin, 1);
            ensureInventoryLoaded();
            cachedOwnedPaddleSkins.add(skin);
        }

        public synchronized boolean hasPaddleSkin(String skin) {
            if ("paddle_Default".equals(skin)) {
                return true;
            }
            ensureInventoryLoaded();
            return cachedOwnedPaddleSkins.contains(skin);
        }

        public synchronized String getEquippedPaddleSkin() {
            ensureInventoryLoaded();
            return cachedEquippedPaddleSkin;
        }

        public synchronized void setEquippedPaddleSkin(String skin) {
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(id);
            for (InventoryManager.InventoryItem item : items) {
                if (item.getItemId().startsWith("paddle:equipped:")) {
                    InventoryManager.removeItem(id, item.getItemId());
                }
            }
            InventoryManager.addItem(id, "paddle:equipped:" + skin, 1);
            ensureInventoryLoaded();
            cachedEquippedPaddleSkin = skin;
        }

        public synchronized void addOwnedBallSkin(String skin) {
            InventoryManager.addItem(id, "ball:" + skin, 1);
            ensureInventoryLoaded();
            cachedOwnedBallSkins.add(skin);
        }

        public synchronized boolean hasBallSkin(String skin) {
            if ("Default".equals(skin)) {
                return true;
            }
            ensureInventoryLoaded();
            return cachedOwnedBallSkins.contains(skin);
        }

        public synchronized String getEquippedBallSkin() {
            ensureInventoryLoaded();
            return cachedEquippedBallSkin;
        }

        public synchronized void setEquippedBallSkin(String skin) {
            List<InventoryManager.InventoryItem> items = InventoryManager.getUserInventory(id);
            for (InventoryManager.InventoryItem item : items) {
                if (item.getItemId().startsWith("skin:equipped:")) {
                    InventoryManager.removeItem(id, item.getItemId());
                }
            }
            InventoryManager.addItem(id, "skin:equipped:" + skin, 1);
            ensureInventoryLoaded();
            cachedEquippedBallSkin = skin;
        }

        public synchronized List<String> getOwnedSkins() {
            ensureInventoryLoaded();
            return new ArrayList<>(cachedOwnedSkins);
        }

        public synchronized List<String> getOwnedPaddleSkins() {
            ensureInventoryLoaded();
            return new ArrayList<>(cachedOwnedPaddleSkins);
        }
    }

    public static boolean isLoggedIn() {
        return currentUserId != null;
    }

    public static void login(int userId, String userName, String passwordHash) {
        currentUserId = userId;
        currentUsername = userName;
        currentUser = new User(userId, userName);

        UserSessionStorage.saveSession(userId, userName, passwordHash);

        syncSettingViewFromSession();
    }

    public static void login(int userId, String userName, String password, boolean isUsingPassword) {
        if (!isUsingPassword) {
            login(userId, userName, password);
            return;
        }
        PasswordHasher passwordHasher = RepositoryFactory.getInstance().getPasswordHasher();
        String hashedPassword = passwordHasher.hashPassword(password);
        login(userId, userName, hashedPassword);
    }

    public static void logout() {
        currentUserId = null;
        currentUsername = null;
        currentUser = null;
        activeProfile = null;

        UserSessionStorage.clearSession();
        syncSettingViewFromSession();
        logger.info("User logged out and session cleared");
    }

    private static void syncSettingViewFromSession() {
        try {
            Object controller = SceneManager.getController("settingView");
            if (controller instanceof SettingView settingView) {
                settingView.syncFromSession();
                logger.debug("Synced SettingView from session");
            }
        } catch (Exception e) {
            logger.debug("SettingView not available for sync");
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean restoreSession() {
        UserSessionStorage.SessionData sessionData = UserSessionStorage.loadSession();
        if (sessionData != null) {
            PlayerProfileManager.ProfileData profile = PlayerProfileManager.getProfile(sessionData.getUserId());
            if (profile != null) {
                currentUserId = sessionData.getUserId();
                currentUsername = sessionData.getUsername();
                currentUser = new User(currentUserId, currentUsername);
                logger.info("Restored session for user: {} (ID: {})", currentUsername, currentUserId);
                return true;
            } else {
                logger.warn("Saved session user no longer exists, clearing session");
                UserSessionStorage.clearSession();
            }
        }
        return false;
    }

    public static void setEquippedSkin(String skin) {
        if (currentUser != null) {
            currentUser.setEquippedSkin(skin);
        } else if (currentUserId != null) {
            PlayerProfileManager.updateSkin(currentUserId, skin);
        }
    }

    public static String getEquippedSkin() {
        if (currentUser != null) {
            return currentUser.getEquippedSkin();
        }
        return "Default";
    }

    public static void setEquippedPaddleSkin(String skin) {
        if (currentUser != null) {
            currentUser.setEquippedPaddleSkin(skin);
        }
    }

    public static String getEquippedPaddleSkin() {
        if (currentUser != null) {
            return currentUser.getEquippedPaddleSkin();
        }
        return "paddle_Default";
    }

    public static String getEquippedBallSkin() {
        if (currentUser != null) {
            return currentUser.getEquippedBallSkin();
        }
        return "Default";
    }

    public static void setEquippedBallSkin(String skinName) {
        if (currentUser != null) {
            currentUser.setEquippedBallSkin(skinName);
            logger.info("Equipped ball skin set to: {}", skinName);
        }
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

    public static void savePlayer(PlayerProfile player) {
        if (currentUser != null) {
            logger.info("Saved user: {} | Money: {} | Equipped skin: {}", currentUser.getUsername(),
                    currentUser.getMoney(), currentUser.getEquippedSkin());
        }
    }
}
