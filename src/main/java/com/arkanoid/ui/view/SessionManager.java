    package com.arkanoid.ui.view;

    import com.arkanoid.systems.player.PlayerProfile;

    import java.util.HashMap;
    import java.util.Map;

    public class SessionManager {
        private static Map<String, String> users = new HashMap<>();
        private static User currentUser = null;
        private static PlayerProfile activeProfile;

        public static class User {
            private String username;
            private int money;
            private Map<String, Boolean> ownedSkins = new HashMap<>();
            private String equippedSkin = "Default";
            private Map<String, Boolean> ownedPaddleSkins = new HashMap<>();
            private String equippedPaddleSkin = "paddle_Default";
            public User(String username) {

                this.username = username;
                this.money = 1000;
                ownedSkins.put("Default", true);
                ownedPaddleSkins.put("paddle_Default", true);
            }

            public String getUsername() {
                return username;
            }
            public int getMoney() {
                return money;
            }

            public void setMoney(int money) {
                this.money = money;
            }

            public void addMoney(int amount) {
                this.money += amount;
            }

            public boolean spendMoney(int amount) {
                if (this.money >= amount) {
                    this.money -= amount;
                    return true;
                }
                return false;
            }
            public void addOwnedSkin(String skin) {
                ownedSkins.put(skin, true);
            }

            public boolean hasSkin(String skin) {
                return ownedSkins.getOrDefault(skin, false);
            }

            public String getEquippedSkin() {
                return equippedSkin;
            }

            public void setEquippedSkin(String skin) {
                this.equippedSkin = skin;
            }

            public void addOwnedPaddleSkin(String skin) { ownedPaddleSkins.put(skin, true); }
            public boolean hasPaddleSkin(String skin) { return ownedPaddleSkins.getOrDefault(skin, false); }
            public String getEquippedPaddleSkin() { return equippedPaddleSkin; }
            public void setEquippedPaddleSkin(String skin) { this.equippedPaddleSkin = skin; }

            public Map<String, Boolean> getOwnedSkins() {
                return ownedSkins;
            }
            public Map<String, Boolean> getOwnedPaddleSkins() {return ownedPaddleSkins;}
        }

        public static boolean register(String username, String password) {
            if (users.containsKey(username)) return false; // user đã tồn tại
            users.put(username, password);
            currentUser = new User(username); // đăng ký xong auto login
            return true;
        }

        public static boolean login(String username, String password) {
            if (!users.containsKey(username)) return false;
            if (!users.get(username).equals(password)) return false;
            currentUser = new User(username);
            return true;
        }
        public static boolean isLoggedIn() {
            return currentUser != null;
        }

        public static void login(User user) {
            currentUser = user;
        }


        public static void logout() {
            currentUser = null;
        }

        public static User getCurrentUser() {
            return currentUser;
        }
        public static void setEquippedSkin(String skin) {
            if (currentUser != null) {
                currentUser.setEquippedSkin(skin);
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
                System.out.println("💾 Đã lưu user: " + currentUser.getUsername() +
                        " | Tiền: " + currentUser.getMoney() +
                        " | Skin đang dùng: " + currentUser.getEquippedSkin());
            }
        }
    }
