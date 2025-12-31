package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.OrderItem;

/**
 * Repository untuk OrderItem data access.
 */
public class OrderItemRepository {

    /**
     * Menyimpan order item ke database.
     */
    public void save(OrderItem orderItem) {
        String sql = "INSERT INTO order_items (id, order_id, item_id, quantity, unit_price, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderItem.getId());
            stmt.setString(2, orderItem.getOrderId());
            stmt.setString(3, orderItem.getItemId());
            stmt.setInt(4, orderItem.getQuantity());
            stmt.setBigDecimal(5, orderItem.getUnitPrice());
            stmt.setBigDecimal(6, orderItem.getSubtotal());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving order item: " + e.getMessage(), e);
        }
    }

    /**
     * Mencari semua order items berdasarkan order ID.
     */
    public java.util.List<OrderItem> findByOrderId(String orderId) {
        java.util.List<OrderItem> items = new java.util.ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getString("id"),
                            rs.getString("order_id"),
                            rs.getString("item_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price")
                    );
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order items: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Mencari nama item pertama dari order dengan join ke tabel items.
     */
    public String findFirstItemNameByOrderId(String orderId) {
        String sql = "SELECT i.name FROM order_items oi "
                + "JOIN items i ON oi.item_id = i.id "
                + "WHERE oi.order_id = ? "
                + "LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding item name: " + e.getMessage(), e);
        }
        return null;
    }
}
