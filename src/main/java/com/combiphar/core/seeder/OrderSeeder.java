package com.combiphar.core.seeder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Seeder for creating test orders with items, payments, and shipments.
 * Defensive programming: Can be run multiple times without duplicates.
 */
public class OrderSeeder {

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   Order Seeder");
        System.out.println("=====================================\n");

        try {
            // Initialize database connection
            DatabaseConfig.getConnection().close();
            System.out.println("✓ Database connection successful\n");

            // Get user IDs and item IDs
            String customer1Id = getUserIdByEmail("customer1@test.com");
            String customer2Id = getUserIdByEmail("customer2@test.com");
            String customer3Id = getUserIdByEmail("customer3@test.com");

            if (customer1Id == null) {
                System.out.println("⚠ No users found. Please run UserSeeder first.");
                System.exit(1);
            }

            String[] itemIds = getAvailableItemIds();
            if (itemIds.length == 0) {
                System.out.println("⚠ No items found. Please run ItemSeeder first.");
                System.exit(1);
            }

            String[] addressIds = getAvailableAddressIds();
            if (addressIds.length == 0) {
                System.out.println("⚠ No addresses found. Please run AddressSeeder first.");
                System.exit(1);
            }

            int count = 0;

            // Create various test orders
            count += createOrder(customer1Id, addressIds[0], "ORD-2025-001",
                    new BigDecimal("3500000"), "TRANSFER", "DELIVERY",
                    "PAID", "COMPLETED",
                    new String[]{ itemIds[0], itemIds[1] },
                    new int[]{ 1, 2 },
                    "BCA", "/images/payments/proof1.jpg",
                    "JNE", "JP1234567890");

            count += createOrder(customer2Id, addressIds[2], "ORD-2025-002",
                    new BigDecimal("2200000"), "TRANSFER", "PICKUP",
                    "PAID", "READY",
                    new String[]{ itemIds[2] },
                    new int[]{ 1 },
                    "MANDIRI", "/images/payments/proof2.jpg",
                    null, null);

            count += createOrder(customer3Id, addressIds[4], "ORD-2025-003",
                    new BigDecimal("5500000"), "TRANSFER", "DELIVERY",
                    "PENDING", "PROCESSING",
                    new String[]{ itemIds[3], itemIds[4] },
                    new int[]{ 1, 1 },
                    null, null,
                    null, null);

            count += createOrder(customer1Id, addressIds[1], "ORD-2025-004",
                    new BigDecimal("1800000"), "TRANSFER", "DELIVERY",
                    "PAID", "SHIPPED",
                    new String[]{ itemIds[5] },
                    new int[]{ 3 },
                    "BRI", "/images/payments/proof3.jpg",
                    "SiCepat", "SC9876543210");

            System.out.println("\n✓ Total orders seeded: " + count);
            System.exit(0);

        } catch (Exception e) {
            System.err.println("✗ Error during seeding: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int createOrder(String userId, String addressId, String orderNumber,
            BigDecimal totalPrice, String paymentMethod, String pickupMethod,
            String paymentStatus, String orderStatus,
            String[] itemIds, int[] quantities,
            String bank, String proof,
            String courierName, String trackingNumber) throws SQLException {

        // Check if order already exists
        if (orderExists(orderNumber)) {
            System.out.println("⚠ Order already exists: " + orderNumber);
            return 0;
        }

        String orderId = UUID.randomUUID().toString();

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Insert order
                String orderSql = "INSERT INTO orders (id, user_id, address_id, order_number, " +
                        "total_price, payment_method, pickup_method, status_payment, status_order) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                    stmt.setString(1, orderId);
                    stmt.setString(2, userId);
                    stmt.setString(3, addressId);
                    stmt.setString(4, orderNumber);
                    stmt.setBigDecimal(5, totalPrice);
                    stmt.setString(6, paymentMethod);
                    stmt.setString(7, pickupMethod);
                    stmt.setString(8, paymentStatus);
                    stmt.setString(9, orderStatus);
                    stmt.executeUpdate();
                }

                // Insert order items
                BigDecimal subtotal = BigDecimal.ZERO;
                for (int i = 0; i < itemIds.length; i++) {
                    String itemId = itemIds[i];
                    int quantity = quantities[i];

                    // Get item price
                    BigDecimal unitPrice = getItemPrice(conn, itemId);
                    BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    subtotal = subtotal.add(itemSubtotal);

                    String orderItemSql = "INSERT INTO order_items (id, order_id, item_id, quantity, unit_price, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement stmt = conn.prepareStatement(orderItemSql)) {
                        stmt.setString(1, UUID.randomUUID().toString());
                        stmt.setString(2, orderId);
                        stmt.setString(3, itemId);
                        stmt.setInt(4, quantity);
                        stmt.setBigDecimal(5, unitPrice);
                        stmt.setBigDecimal(6, itemSubtotal);
                        stmt.executeUpdate();
                    }
                }

                // Insert payment if provided
                if (bank != null) {
                    // Map payment status from orders to payments enum
                    String paymentDbStatus = paymentStatus;
                    if ("PAID".equals(paymentStatus)) {
                        paymentDbStatus = "SUCCESS";
                    }

                    String paymentSql = "INSERT INTO payments (id, order_id, type, bank, proof, amount, status) " +
                            "VALUES (?, ?, 'TRANSFER', ?, ?, ?, ?)";

                    try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                        stmt.setString(1, UUID.randomUUID().toString());
                        stmt.setString(2, orderId);
                        stmt.setString(3, bank);
                        stmt.setString(4, proof);
                        stmt.setBigDecimal(5, totalPrice);
                        stmt.setString(6, paymentDbStatus);
                        stmt.executeUpdate();
                    }
                }

                // Insert shipment if delivery method
                if ("DELIVERY".equals(pickupMethod)) {
                    String shipmentSql = "INSERT INTO shipments (id, order_id, address_id, courier_name, " +
                            "tracking_number, shipment_status) VALUES (?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement stmt = conn.prepareStatement(shipmentSql)) {
                        stmt.setString(1, UUID.randomUUID().toString());
                        stmt.setString(2, orderId);
                        stmt.setString(3, addressId);
                        stmt.setString(4, courierName);
                        stmt.setString(5, trackingNumber);
                        stmt.setString(6, orderStatus.equals("COMPLETED") ? "RECEIVED" :
                                orderStatus.equals("SHIPPED") ? "SHIPPED" : "PROCESSING");
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("✓ Order created: " + orderNumber + " - " + paymentStatus + "/" + orderStatus);
                return 1;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static boolean orderExists(String orderNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_number = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static BigDecimal getItemPrice(Connection conn, String itemId) throws SQLException {
        String sql = "SELECT price FROM items WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("price");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private static String[] getAvailableItemIds() throws SQLException {
        String sql = "SELECT id FROM items WHERE is_published = true AND stock > 0 LIMIT 10";
        java.util.List<String> ids = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        }
        return ids.toArray(new String[0]);
    }

    private static String[] getAvailableAddressIds() throws SQLException {
        String sql = "SELECT id FROM addresses LIMIT 10";
        java.util.List<String> ids = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        }
        return ids.toArray(new String[0]);
    }

    private static String getUserIdByEmail(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id");
                }
            }
        }
        return null;
    }
}
