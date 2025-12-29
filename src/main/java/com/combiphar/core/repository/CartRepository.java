package com.combiphar.core.repository;

import java.math.BigDecimal;
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
 * Simple repository to persist/load a user's cart. Designed to be minimal,
 * defensive and follow the project's DB access style (uses DatabaseConfig).
 */
public class CartRepository {

    /**
     * Loads cart for a given user if exists.
     */
    public Optional<Cart> findByUserId(String userId) throws SQLException {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT id FROM carts WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    String cartId = rs.getString("id");

                    // Now load items
                    Cart cart = new Cart();
                    String itemsSql = "SELECT ci.item_id, ci.quantity, ci.price, i.name "
                            + "FROM cart_items ci LEFT JOIN items i ON ci.item_id = i.id "
                            + "WHERE ci.cart_id = ?";
                    try (PreparedStatement ips = conn.prepareStatement(itemsSql)) {
                        ips.setString(1, cartId);
                        try (ResultSet irs = ips.executeQuery()) {
                            while (irs.next()) {
                                String itemId = irs.getString("item_id");
                                String itemName = irs.getString("name");
                                BigDecimal price = irs.getBigDecimal("price");
                                int qty = irs.getInt("quantity");
                                cart.addItem(new CartItem(itemId, itemName != null ? itemName : "", price, qty));
                            }
                        }
                    }
                    return Optional.of(cart);
                }
            }
        }
    }

    /**
     * Persists the given cart for the user. This is best-effort and
     * transactional.
     */
    public void saveCartForUser(String userId, Cart cart) throws SQLException {
        if (userId == null || userId.isBlank() || cart == null) {
            throw new IllegalArgumentException("userId and cart are required");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String findSql = "SELECT id FROM carts WHERE user_id = ?";
                String cartId = null;
                try (PreparedStatement fps = conn.prepareStatement(findSql)) {
                    fps.setString(1, userId);
                    try (ResultSet frs = fps.executeQuery()) {
                        if (frs.next()) {
                            cartId = frs.getString("id");
                        }
                    }
                }

                if (cartId == null) {
                    cartId = UUID.randomUUID().toString();
                    String insertCart = "INSERT INTO carts (id, user_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ips = conn.prepareStatement(insertCart)) {
                        Timestamp now = Timestamp.from(Instant.now());
                        ips.setString(1, cartId);
                        ips.setString(2, userId);
                        ips.setTimestamp(3, now);
                        ips.setTimestamp(4, now);
                        ips.executeUpdate();
                    }
                } else {
                    String upd = "UPDATE carts SET updated_at = ? WHERE id = ?";
                    try (PreparedStatement ups = conn.prepareStatement(upd)) {
                        ups.setTimestamp(1, Timestamp.from(Instant.now()));
                        ups.setString(2, cartId);
                        ups.executeUpdate();
                    }
                }

                // Remove existing items and re-insert (simple, clear approach)
                try (PreparedStatement dps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                    dps.setString(1, cartId);
                    dps.executeUpdate();
                }

                String insertItem = "INSERT INTO cart_items (cart_id, item_id, quantity, price, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ips = conn.prepareStatement(insertItem)) {
                    Timestamp now = Timestamp.from(Instant.now());
                    for (CartItem ci : cart.getItems()) {
                        ips.setString(1, cartId);
                        ips.setString(2, ci.getItemId());
                        ips.setInt(3, ci.getQuantity());
                        ips.setBigDecimal(4, ci.getItemPrice());
                        ips.setTimestamp(5, now);
                        ips.setTimestamp(6, now);
                        ips.addBatch();
                    }
                    ips.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
