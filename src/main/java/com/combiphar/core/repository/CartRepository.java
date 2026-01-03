package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Cart;
import com.combiphar.core.model.CartItem;

/**
 * Repository to persist/load a user's cart. Minimal and defensive.
 */
public class CartRepository {

    public Optional<Cart> findByUserId(String userId) throws SQLException {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String cartId = findCartId(conn, userId);
            if (cartId == null) {
                return Optional.empty();
            }
            return Optional.of(loadCartItems(conn, cartId));
        }
    }

    public void saveCartForUser(String userId, Cart cart) throws SQLException {
        if (userId == null || userId.isBlank() || cart == null) {
            throw new IllegalArgumentException("userId and cart are required");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String cartId = findCartId(conn, userId);
                cartId = cartId != null ? updateCart(conn, cartId) : createCart(conn, userId);
                replaceItems(conn, cartId, cart);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String findCartId(Connection conn, String userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM carts WHERE user_id = ?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("id") : null;
            }
        }
    }

    private Cart loadCartItems(Connection conn, String cartId) throws SQLException {
        Cart cart = new Cart();
        String sql = "SELECT ci.item_id, ci.quantity, ci.price, i.name, i.image_url "
                + "FROM cart_items ci LEFT JOIN items i ON ci.item_id = i.id WHERE ci.cart_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cart.addItem(new CartItem(
                            rs.getString("item_id"),
                            rs.getString("name") != null ? rs.getString("name") : "",
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getString("image_url")));
                }
            }
        }
        return cart;
    }

    private String createCart(Connection conn, String userId) throws SQLException {
        String cartId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO carts (id, user_id, created_at, updated_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, cartId);
            ps.setString(2, userId);
            ps.setTimestamp(3, now);
            ps.setTimestamp(4, now);
            ps.executeUpdate();
        }
        return cartId;
    }

    private String updateCart(Connection conn, String cartId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE carts SET updated_at = ? WHERE id = ?")) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setString(2, cartId);
            ps.executeUpdate();
        }
        return cartId;
    }

    public void clearCartForUser(String userId) throws SQLException {
        if (userId == null || userId.isBlank()) {
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String cartId = findCartId(conn, userId);
            if (cartId != null) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                    ps.setString(1, cartId);
                    ps.executeUpdate();
                }
            }
        }
    }

    private void replaceItems(Connection conn, String cartId, Cart cart) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
            ps.setString(1, cartId);
            ps.executeUpdate();
        }

        String sql = "INSERT INTO cart_items (cart_id, item_id, quantity, price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Timestamp now = Timestamp.from(Instant.now());
            for (CartItem ci : cart.getItems()) {
                ps.setString(1, cartId);
                ps.setString(2, ci.getItemId());
                ps.setInt(3, ci.getQuantity());
                ps.setBigDecimal(4, ci.getItemPrice());
                ps.setTimestamp(5, now);
                ps.setTimestamp(6, now);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
