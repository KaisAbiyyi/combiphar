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
                    if (revenue == null) revenue = BigDecimal.ZERO;
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
                    
                    // Calculate simulated YoY growth (random for demo, replace with actual calculation)
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
}
