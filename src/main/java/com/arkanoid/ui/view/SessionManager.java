package com.arkanoid.ui.view;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static Map<String, String> users = new HashMap<>();
    private static User currentUser = null;

    // Lớp User giả định, bạn có thể thay thế bằng lớp User thực tế của mình
    public static class User {
        private String username;
        public User(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
        // Thêm getters cho các trường khác
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

    /**
     * Đăng xuất người dùng
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    public static User getCurrentUser() {
        return currentUser;
    }
}
