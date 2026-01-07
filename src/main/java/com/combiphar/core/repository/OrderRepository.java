package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Order;

/**
 * Repository untuk Order data access. Single Responsibility: hanya menangani
 * database operations untuk Order.
 */
public class OrderRepository {

    /**
     * Menyimpan order baru ke database.
     */
    public void save(Order order) {
        String sql = "INSERT INTO orders (id, user_id, address_id, order_number, total_price, "
                + "payment_method, pickup_method, status_payment, status_order, note, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, order.getId());
            stmt.setString(2, order.getUserId());
            stmt.setString(3, order.getAddressId());
            stmt.setString(4, order.getOrderNumber());
            stmt.setBigDecimal(5, order.getTotalPrice());
            stmt.setString(6, order.getPaymentMethod());
            stmt.setString(7, order.getPickupMethod());
            stmt.setString(8, order.getStatusPayment());
            stmt.setString(9, order.getStatusOrder());
            stmt.setString(10, order.getNote());
            stmt.setTimestamp(11, Timestamp.valueOf(order.getCreatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving order: " + e.getMessage(), e);
        }
    }

    /**
     * Mencari order berdasarkan ID.
     */
    public Optional<Order> findById(String id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Update status pembayaran order.
     */
    public void updatePaymentStatus(String orderId, String status) {
        String sql = "UPDATE orders SET status_payment = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating payment status: " + e.getMessage(), e);
        }
    }

    /**
     * Update status order.
     */
    public void updateOrderStatus(String orderId, String status) {
        String sql = "UPDATE orders SET status_order = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating order status: " + e.getMessage(), e);
        }
    }

    /**
     * Mencari semua order milik user, diurutkan dari yang terbaru.
     */
    public java.util.List<Order> findByUserId(String userId) {
        java.util.List<Order> orders = new java.util.ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders by user: " + e.getMessage(), e);
        }
        return orders;
    }

    /**
     * Mencari semua order.
     */
    public java.util.List<Order> findAll() {
        java.util.List<Order> orders = new java.util.ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all orders: " + e.getMessage(), e);
        }
        return orders;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        return new Order(
                rs.getString("id"),
                rs.getString("user_id"),
                rs.getString("address_id"),
                rs.getString("order_number"),
                rs.getBigDecimal("total_price"),
                rs.getString("payment_method"),
                rs.getString("pickup_method"),
                rs.getString("status_payment"),
                rs.getString("status_order"),
                rs.getString("note"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
