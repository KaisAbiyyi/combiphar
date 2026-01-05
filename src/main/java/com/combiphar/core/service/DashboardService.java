package com.combiphar.core.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Service for admin dashboard statistics and metrics.
 */
public class DashboardService {

    /**
     * Get all dashboard statistics for admin dashboard page.
     * 
     * @return Map with all dashboard stats
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Penjualan (Total Revenue from paid orders)
        Map<String, Object> salesStats = getSalesStats();
        stats.putAll(salesStats);

        // 2. Barang Terjual (Total Units Sold)
        Map<String, Object> unitsStats = getUnitsSoldStats();
        stats.putAll(unitsStats);

        // 3. User Aktif (Active Users in last 30 days)
        Map<String, Object> activeUsersStats = getActiveUsersStats();
        stats.putAll(activeUsersStats);

        // 4. Persentase Pengiriman (Shipment Success Rate)
        Map<String, Object> shipmentStats = getShipmentStats();
        stats.putAll(shipmentStats);

        return stats;
    }

    /**
     * Get sales statistics (Penjualan).
     * Calculates total revenue from paid orders and growth percentage.
     * 
     * @return Map with sales data
     */
    private Map<String, Object> getSalesStats() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal previousYearRevenue = BigDecimal.ZERO;
        int currentYear = LocalDateTime.now().getYear();

        // Get current year revenue (2026)
        String currentSql = """
                    SELECT SUM(total_price) as revenue
                    FROM orders
                    WHERE status_payment = 'PAID'
                    AND YEAR(created_at) = ?
                """;

        // Get previous year revenue (2025) for comparison
        String previousSql = """
                    SELECT SUM(total_price) as revenue
                    FROM orders
                    WHERE status_payment = 'PAID'
                    AND YEAR(created_at) = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Current year
            try (PreparedStatement stmt = conn.prepareStatement(currentSql)) {
                stmt.setInt(1, currentYear);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalRevenue = rs.getBigDecimal("revenue");
                        if (totalRevenue == null)
                            totalRevenue = BigDecimal.ZERO;
                    }
                }
            }

            // Previous year
            try (PreparedStatement stmt = conn.prepareStatement(previousSql)) {
                stmt.setInt(1, currentYear - 1);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        previousYearRevenue = rs.getBigDecimal("revenue");
                        if (previousYearRevenue == null)
                            previousYearRevenue = BigDecimal.ZERO;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sales stats: " + e.getMessage());
        }

        // Calculate growth percentage
        double growthPercentage = 0.0;
        if (previousYearRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = totalRevenue.subtract(previousYearRevenue);
            growthPercentage = growth.divide(previousYearRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthPercentage = 100.0; // If no previous data but current has revenue
        }

        stats.put("totalSales", totalRevenue);
        stats.put("totalSalesDisplay", formatCurrency(totalRevenue));
        stats.put("salesGrowth", growthPercentage);
        stats.put("salesGrowthDisplay", String.format("%.0f%% dari", Math.abs(growthPercentage)));
        stats.put("salesGrowthDirection", growthPercentage >= 0 ? "up" : "down");

        // Update footer with current year
        stats.put("salesFooter", String.format("Penjualan semua gudang tahun %d", currentYear));

        return stats;
    }

    /**
     * Get units sold statistics (Barang Terjual).
     * Calculates total quantity of items sold.
     * 
     * @return Map with units sold data
     */
    private Map<String, Object> getUnitsSoldStats() {
        Map<String, Object> stats = new HashMap<>();
        int totalUnits = 0;
        int totalOrders = 0;
        int previousYearUnits = 0;
        int currentYear = LocalDateTime.now().getYear();

        // Current year units (2026)
        String currentSql = """
                    SELECT
                        SUM(oi.quantity) as total_units,
                        COUNT(DISTINCT o.id) as total_orders
                    FROM order_items oi
                    JOIN orders o ON oi.order_id = o.id
                    WHERE o.status_payment = 'PAID'
                    AND YEAR(o.created_at) = ?
                """;

        // Previous year units (2025) for comparison
        String previousSql = """
                    SELECT SUM(oi.quantity) as total_units
                    FROM order_items oi
                    JOIN orders o ON oi.order_id = o.id
                    WHERE o.status_payment = 'PAID'
                    AND YEAR(o.created_at) = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Current year
            try (PreparedStatement stmt = conn.prepareStatement(currentSql)) {
                stmt.setInt(1, currentYear);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalUnits = rs.getInt("total_units");
                        totalOrders = rs.getInt("total_orders");
                    }
                }
            }

            // Previous year
            try (PreparedStatement stmt = conn.prepareStatement(previousSql)) {
                stmt.setInt(1, currentYear - 1);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        previousYearUnits = rs.getInt("total_units");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching units sold stats: " + e.getMessage());
        }

        // Calculate difference and percentage
        int unitsDifference = totalUnits - previousYearUnits;
        double changePercentage = 0.0;
        if (previousYearUnits > 0) {
            changePercentage = ((double) unitsDifference / previousYearUnits) * 100;
        } else if (totalUnits > 0) {
            changePercentage = 100.0;
        }

        String changeText;
        if (unitsDifference >= 0) {
            changeText = String.format("%.0f%% dari tahun lalu", Math.abs(changePercentage));
        } else {
            changeText = String.format("%.0f%% dari tahun lalu", Math.abs(changePercentage));
        }

        String footerText = String.format("Terdapat %d unit dalam %d pesanan berhasil tahun %d",
                totalUnits, totalOrders, currentYear);

        stats.put("totalUnits", totalUnits);
        stats.put("totalUnitsDisplay", String.format("%,d", totalUnits));
        stats.put("unitsChange", changeText);
        stats.put("unitsChangeDirection", unitsDifference >= 0 ? "up" : "down");
        stats.put("unitsFooter", footerText);

        return stats;
    }

    /**
     * Get active users statistics (User Aktif).
     * Counts users who made orders within last 30 days.
     * 
     * @return Map with active users data
     */
    private Map<String, Object> getActiveUsersStats() {
        Map<String, Object> stats = new HashMap<>();
        int activeUsers = 0;
        int totalUsers = 0;

        // Count active users (who made orders in last 30 days)
        String activeSql = """
                    SELECT COUNT(DISTINCT o.user_id) as active_count
                    FROM orders o
                    WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                """;

        // Count total users with CUSTOMER role
        String totalSql = "SELECT COUNT(id) as total_count FROM users WHERE role = 'CUSTOMER'";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Active users
            try (PreparedStatement stmt = conn.prepareStatement(activeSql);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    activeUsers = rs.getInt("active_count");
                }
            }

            // Total users
            try (PreparedStatement stmt = conn.prepareStatement(totalSql);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalUsers = rs.getInt("total_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active users stats: " + e.getMessage());
        }

        // Calculate percentage
        double activePercentage = 0.0;
        if (totalUsers > 0) {
            activePercentage = (double) activeUsers / totalUsers * 100;
        }

        stats.put("activeUsers", activeUsers);
        stats.put("activeUsersDisplay", String.format("%d", activeUsers));
        stats.put("activeUsersChange", String.format("%d dari total user", totalUsers));
        stats.put("activeUsersChangeDirection", "up");
        stats.put("activeUsersFooter", "Pengguna dengan status aktif 30 hari terakhir");

        return stats;
    }

    /**
     * Get shipment statistics (Persentase Pengiriman).
     * Calculates percentage of successful deliveries.
     * 
     * @return Map with shipment data
     */
    private Map<String, Object> getShipmentStats() {
        Map<String, Object> stats = new HashMap<>();
        int totalShipments = 0;
        int deliveredShipments = 0;

        String sql = """
                    SELECT
                        COUNT(*) as total_shipments,
                        SUM(CASE WHEN shipment_status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered_count
                    FROM shipments
                """;

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                totalShipments = rs.getInt("total_shipments");
                deliveredShipments = rs.getInt("delivered_count");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching shipment stats: " + e.getMessage());
        }

        // Calculate percentage
        double deliveryRate = 0.0;
        if (totalShipments > 0) {
            deliveryRate = (double) deliveredShipments / totalShipments * 100;
        }

        // Calculate how much above/below target (assume 95% target)
        double target = 95.0;
        double differenceFromTarget = deliveryRate - target;

        stats.put("deliveryRate", deliveryRate);
        stats.put("deliveryRateDisplay", String.format("%.0f%%", deliveryRate));
        stats.put("deliveryChange", String.format("%.0f%% dari target", Math.abs(differenceFromTarget)));
        stats.put("deliveryChangeDirection", differenceFromTarget >= 0 ? "up" : "down");
        stats.put("deliveryFooter", String.format("Pengiriman tepat waktu untuk %d pesanan", totalShipments));

        return stats;
    }

    /**
     * Format currency to Indonesian display format.
     * Uses Indonesian abbreviations: Jt (Juta/Million), M (Miliar/Billion), T
     * (Triliun/Trillion)
     * 
     * @param amount BigDecimal amount
     * @return Formatted string
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "Rp 0";
        }

        double value = amount.doubleValue();

        // Triliun (Trillion) - 1,000,000,000,000+
        if (value >= 1_000_000_000_000.0) {
            double trillions = value / 1_000_000_000_000.0;
            return String.format("Rp %.1f T", trillions);
        }
        // Miliar (Billion) - 1,000,000,000+
        else if (value >= 1_000_000_000.0) {
            double billions = value / 1_000_000_000.0;
            return String.format("Rp %.1f M", billions);
        }
        // Juta (Million) - 1,000,000+
        else if (value >= 1_000_000.0) {
            double millions = value / 1_000_000.0;
            return String.format("Rp %.1f Jt", millions);
        }
        // Ribu (Thousand) - 1,000+
        else if (value >= 1_000.0) {
            double thousands = value / 1_000.0;
            return String.format("Rp %.1f Rb", thousands);
        }
        // Less than 1,000
        else {
            return String.format("Rp %.0f", value);
        }
    }

    /**
     * Get recent transactions for dashboard.
     * Returns last 5 transactions with order details.
     * 
     * @return List of recent transactions
     */
    public List<Map<String, Object>> getRecentTransactions() {
        List<Map<String, Object>> transactions = new ArrayList<>();

        String sql = """
                    SELECT
                        o.order_number as invoice,
                        u.name as customer_name,
                        o.total_price,
                        o.status_order,
                        o.status_payment,
                        o.created_at,
                        GROUP_CONCAT(
                            CONCAT(i.name, ' (', oi.quantity, ')')
                            SEPARATOR ', '
                        ) as items
                    FROM orders o
                    JOIN users u ON o.user_id = u.id
                    LEFT JOIN order_items oi ON o.id = oi.order_id
                    LEFT JOIN items i ON oi.item_id = i.id
                    WHERE DATE(o.created_at) = CURDATE()
                    GROUP BY o.id
                    ORDER BY o.created_at DESC
                    LIMIT 5
                """;

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

            while (rs.next()) {
                Map<String, Object> transaction = new HashMap<>();

                transaction.put("invoice", rs.getString("invoice"));
                transaction.put("customerName", rs.getString("customer_name"));
                transaction.put("items", rs.getString("items"));
                transaction.put("totalPrice", rs.getBigDecimal("total_price"));
                transaction.put("totalPriceDisplay", formatCurrency(rs.getBigDecimal("total_price")));

                // Map status order to badge display
                String statusOrder = rs.getString("status_order");
                String statusPayment = rs.getString("status_payment");
                Map<String, String> statusInfo = mapOrderStatus(statusOrder, statusPayment);
                transaction.put("statusText", statusInfo.get("text"));
                transaction.put("statusClass", statusInfo.get("class"));

                // Format timestamp
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                transaction.put("timestamp", createdAt.format(formatter));

                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recent transactions: " + e.getMessage());
        }

        return transactions;
    }

    /**
     * Map order status to display text and badge class.
     */
    private Map<String, String> mapOrderStatus(String statusOrder, String statusPayment) {
        Map<String, String> result = new HashMap<>();

        // Check payment status first
        if ("PENDING".equals(statusPayment)) {
            result.put("text", "Menunggu Pembayaran");
            result.put("class", "badge--pending");
            return result;
        }

        if ("FAILED".equals(statusPayment)) {
            result.put("text", "Pembayaran Gagal");
            result.put("class", "badge--dibatalkan");
            return result;
        }

        // If paid, check order status
        switch (statusOrder) {
            case "PENDING":
                result.put("text", "Pesanan Baru");
                result.put("class", "badge--pending");
                break;
            case "PROCESSING":
                result.put("text", "Diproses");
                result.put("class", "badge--dikemas");
                break;
            case "READY":
                result.put("text", "Menunggu Pickup");
                result.put("class", "badge--dikemas");
                break;
            case "SHIPPED":
                result.put("text", "Proses Kirim");
                result.put("class", "badge--proses");
                break;
            case "DELIVERED":
                result.put("text", "Terkirim");
                result.put("class", "badge--selesai");
                break;
            case "COMPLETED":
                result.put("text", "Selesai");
                result.put("class", "badge--selesai");
                break;
            case "CANCELLED":
                result.put("text", "Dibatalkan");
                result.put("class", "badge--dibatalkan");
                break;
            default:
                result.put("text", statusOrder);
                result.put("class", "badge--pending");
        }

        return result;
    }
}
