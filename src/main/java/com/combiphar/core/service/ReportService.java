package com.combiphar.core.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Service for report/laporan statistics and analytics.
 * Handles revenue calculations, top categories, and performance metrics.
 */
public class ReportService {

    /**
     * Get top categories by revenue from completed orders.
     * 
     * @param limit Number of top categories to return
     * @return List of category statistics with revenue, percentage, and growth
     */
    public List<Map<String, Object>> getTopCategories(int limit) {
        List<Map<String, Object>> categories = new ArrayList<>();

        String sql = """
                    SELECT
                        c.id,
                        c.name,
                        COUNT(DISTINCT o.id) as order_count,
                        SUM(oi.subtotal) as total_revenue,
                        SUM(oi.quantity) as total_units
                    FROM categories c
                    INNER JOIN items i ON i.category_id = c.id
                    INNER JOIN order_items oi ON oi.item_id = i.id
                    INNER JOIN orders o ON o.id = oi.order_id
                    WHERE o.status_payment = 'PAID'
                    GROUP BY c.id, c.name
                    ORDER BY total_revenue DESC
                    LIMIT ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                // First pass: calculate total revenue for percentage
                BigDecimal grandTotal = BigDecimal.ZERO;
                List<Map<String, Object>> tempData = new ArrayList<>();

                while (rs.next()) {
                    Map<String, Object> category = new HashMap<>();
                    category.put("id", rs.getString("id"));
                    category.put("name", rs.getString("name"));
                    category.put("orderCount", rs.getInt("order_count"));

                    BigDecimal revenue = rs.getBigDecimal("total_revenue");
                    if (revenue == null)
                        revenue = BigDecimal.ZERO;
                    category.put("totalRevenue", revenue.doubleValue());
                    category.put("totalUnits", rs.getInt("total_units"));

                    grandTotal = grandTotal.add(revenue);
                    tempData.add(category);
                }

                // Second pass: calculate percentages and format
                for (Map<String, Object> category : tempData) {
                    BigDecimal revenue = BigDecimal.valueOf((Double) category.get("totalRevenue"));

                    // Calculate percentage
                    double percentage = 0.0;
                    if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = revenue.divide(grandTotal, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    }
                    category.put("revenuePercentage", percentage);

                    // Format revenue display (in millions)
                    double revenueInMillions = revenue.divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP)
                            .doubleValue();
                    category.put("revenueDisplay", String.format("Rp %.1fM", revenueInMillions));

                    // Format meta display
                    category.put("metaDisplay", String.format("Rp %.1fM • %.0f%% revenue",
                            revenueInMillions, percentage));

                    // Calculate simulated YoY growth (random for demo, replace with actual
                    // calculation)
                    // In production, this should compare with previous year data
                    double yoyGrowth = calculateYoYGrowth(revenue);
                    category.put("yoyGrowth", yoyGrowth);
                    category.put("yoyGrowthDisplay", String.format("%+.0f%% YoY", yoyGrowth));

                    // Determine badge class based on growth
                    if (yoyGrowth >= 0) {
                        category.put("badgeClass", "category-item__badge--success");
                    } else {
                        category.put("badgeClass", "category-item__badge--warning");
                    }

                    categories.add(category);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching top categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    /**
     * Calculate Year-over-Year growth.
     * For demo purposes, this generates simulated growth based on revenue.
     * In production, replace with actual historical data comparison.
     * 
     * @param currentRevenue Current revenue
     * @return YoY growth percentage
     */
    private double calculateYoYGrowth(BigDecimal currentRevenue) {
        // Simulated YoY growth for demonstration
        // Higher revenue categories tend to have positive growth
        double revenue = currentRevenue.doubleValue();

        if (revenue > 20_000_000) {
            return 18.0; // +18% for top performer
        } else if (revenue > 15_000_000) {
            return 9.0; // +9% for second
        } else if (revenue > 8_000_000) {
            return -3.0; // -3% for third
        } else {
            return 5.0; // +5% for others
        }

        // TODO: Replace with actual query:
        // SELECT SUM(oi.subtotal) FROM order_items oi
        // JOIN orders o ON o.id = oi.order_id
        // WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
        // AND o.created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR) + INTERVAL 1 MONTH
    }

    /**
     * Get total revenue from all paid orders.
     * 
     * @return Total revenue
     */
    public BigDecimal getTotalRevenue() {
        String sql = """
                    SELECT SUM(total_price) as total
                    FROM orders
                    WHERE status_payment = 'PAID'
                """;

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }

        } catch (SQLException e) {
            System.err.println("Error fetching total revenue: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    /**
     * Get total units sold from completed orders.
     * 
     * @return Total units
     */
    public int getTotalUnitsSold() {
        String sql = """
                    SELECT SUM(oi.quantity) as total
                    FROM order_items oi
                    INNER JOIN orders o ON o.id = oi.order_id
                    WHERE o.status_payment = 'PAID'
                """;

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching total units: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get dashboard statistics including revenue, units, GMV, and margin.
     * 
     * @return Map with all dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Calculate total revenue
        BigDecimal totalRevenue = getTotalRevenue();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalRevenueDisplay", formatCurrency(totalRevenue));
        
        // Calculate total units sold
        int totalUnits = getTotalUnitsSold();
        stats.put("totalUnits", totalUnits);
        stats.put("totalUnitsDisplay", String.format("%,d", totalUnits));
        
        // Calculate total paid orders count
        int totalOrders = getTotalPaidOrders();
        stats.put("totalOrders", totalOrders);
        
        // Calculate GMV per order (average order value)
        BigDecimal gmvPerOrder = BigDecimal.ZERO;
        if (totalOrders > 0) {
            gmvPerOrder = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }
        stats.put("gmvPerOrder", gmvPerOrder);
        stats.put("gmvPerOrderDisplay", formatCurrency(gmvPerOrder));
        
        // Calculate average units per month (assuming last 12 months)
        double avgUnitsPerMonth = totalUnits / 12.0;
        stats.put("avgUnitsPerMonth", String.format("%.0f", avgUnitsPerMonth));
        
        // Calculate net margin (simplified: assume 36% margin)
        // In production, calculate from actual cost data
        double netMargin = 36.0;
        stats.put("netMargin", netMargin);
        stats.put("netMarginDisplay", String.format("%.0f%%", netMargin));
        
        // Calculate revenue growth (comparing to simulated previous period)
        double revenueGrowth = calculateRevenueGrowth(totalRevenue);
        stats.put("revenueGrowth", revenueGrowth);
        stats.put("revenueGrowthDisplay", String.format("%+.0f%%", revenueGrowth));
        
        return stats;
    }

    /**
     * Get total count of paid orders.
     * 
     * @return Total order count
     */
    private int getTotalPaidOrders() {
        String sql = """
                SELECT COUNT(*) as total
                FROM orders
                WHERE status_payment = 'PAID'
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching total orders: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Format currency to display format (Rp X,XM or Rp X,XXX).
     * 
     * @param amount Amount to format
     * @return Formatted string
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "Rp 0";
        }
        
        // If amount >= 1 million, show in millions
        if (amount.compareTo(BigDecimal.valueOf(1_000_000)) >= 0) {
            double millions = amount.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP).doubleValue();
            return String.format("Rp %.1fM", millions);
        }
        
        // Otherwise show in thousands
        double thousands = amount.divide(BigDecimal.valueOf(1_000), 0, RoundingMode.HALF_UP).doubleValue();
        return String.format("Rp %,.0fK", thousands);
    }

    /**
     * Calculate revenue growth compared to simulated previous period.
     * In production, this should compare with actual historical data.
     * 
     * @param currentRevenue Current total revenue
     * @return Growth percentage
     */
    private double calculateRevenueGrowth(BigDecimal currentRevenue) {
        // Simulated: assume previous period had 80% of current revenue
        // This represents +25% growth (100/80 - 1 = 0.25)
        // In production, query actual previous period revenue
        
        // For demo: if revenue > 50M, show +24% growth
        // Otherwise calculate based on revenue scale
        double revenue = currentRevenue.doubleValue();
        
        if (revenue >= 50_000_000) {
            return 24.0; // +24% vs Q2 2025
        } else if (revenue >= 30_000_000) {
            return 18.0;
        } else if (revenue >= 10_000_000) {
            return 12.0;
        } else {
            return 8.0;
        }
        
        // TODO: Replace with actual query:
        // SELECT SUM(total_price) FROM orders
        // WHERE status_payment = 'PAID'
        // AND created_at BETWEEN ? AND ?
        // (previous quarter or year)
    }
}
