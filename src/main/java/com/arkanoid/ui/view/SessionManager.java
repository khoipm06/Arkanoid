package com.arkanoid.ui.view;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static Map<String, String> users = new HashMap<>();
    private static User currentUser = null;

    public static class User {
        private String username;
        public User(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

    public static boolean register(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, password);
        currentUser = new User(username);
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
}
