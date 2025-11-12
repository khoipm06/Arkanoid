package com.arkanoid.database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for inventory operations
 */
public class InventoryManager {
    private static final DatabaseManager databaseManager = DatabaseManager.getInstance();

    public static class InventoryItem {
        private final int id;
        private final int userId;
        private final String itemId;
        private final int quantity;
        private final LocalDateTime purchasedAt;

        public InventoryItem(int id, int userId, String itemId, int quantity, LocalDateTime purchasedAt) {
            this.id = id;
            this.userId = userId;
            this.itemId = itemId;
            this.quantity = quantity;
            this.purchasedAt = purchasedAt;
        }

        public int getId() {
            return id;
        }

        public int getUserId() {
            return userId;
        }

        public String getItemId() {
            return itemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public LocalDateTime getPurchasedAt() {
            return purchasedAt;
        }
    }

    /**
     * Add an item to user's inventory
     */
    public static boolean addItem(int userId, String itemId, int quantity) {
        String checkSql = "SELECT id, quantity FROM inventory WHERE user_id = ? AND item_id = ?";
        String updateSql = "UPDATE inventory SET quantity = quantity + ? WHERE id = ?";
        String insertSql = "INSERT INTO inventory (user_id, item_id, quantity, purchased_at) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                checkStmt.setString(2, itemId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Item exists, update quantity
                        int existingId = rs.getInt("id");
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, quantity);
                            updateStmt.setInt(2, existingId);
                            return updateStmt.executeUpdate() > 0;
                        }
                    } else {
                        // New item, insert
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, userId);
                            insertStmt.setString(2, itemId);
                            insertStmt.setInt(3, quantity);
                            insertStmt.setString(4, LocalDateTime.now().toString());
                            return insertStmt.executeUpdate() > 0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return false;
    }

    /**
     * Get all items in user's inventory
     */
    public static List<InventoryItem> getUserInventory(int userId) {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE user_id = ? ORDER BY purchased_at DESC";

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        items.add(new InventoryItem(
                                rs.getInt("id"),
                                rs.getInt("user_id"),
                                rs.getString("item_id"),
                                rs.getInt("quantity"),
                                LocalDateTime.parse(rs.getString("purchased_at"))));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return items;
    }

    /**
     * Check if user owns a specific item
     */
    public static boolean hasItem(int userId, String itemId) {
        String sql = "SELECT COUNT(*) FROM inventory WHERE user_id = ? AND item_id = ?";

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, itemId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return false;
    }

    /**
     * Get quantity of a specific item
     */
    public static int getItemQuantity(int userId, String itemId) {
        String sql = "SELECT quantity FROM inventory WHERE user_id = ? AND item_id = ?";

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, itemId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("quantity");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return 0;
    }

    /**
     * Remove an item from inventory
     */
    public static boolean removeItem(int userId, String itemId) {
        String sql = "DELETE FROM inventory WHERE user_id = ? AND item_id = ?";

        Connection conn = null;
        try {
            conn = databaseManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, itemId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                databaseManager.releaseConnection(conn);
            }
        }
        return false;
    }
}
