package com.combiphar.core.seeder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Seeder to insert dummy data for year 2026
 */
public class Data2026Seeder {

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   2026 Data Seeder");
        System.out.println("=====================================\n");

        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("✓ Database connection successful\n");

            // Read SQL file
            String sqlFile = "database/dummy_data_2026.sql";
            String content = Files.readString(Paths.get(sqlFile));

            // Split by semicolon and execute each statement
            String[] statements = content.split(";");

            int successCount = 0;
            int skipCount = 0;

            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    sql = sql.trim();

                    // Skip empty statements and comments
                    if (sql.isEmpty() || sql.startsWith("--") || sql.startsWith("SELECT '===")
                            || sql.startsWith("SELECT 'Summary") || sql.startsWith("SELECT CONCAT")) {
                        continue;
                    }

                    try {
                        stmt.execute(sql);
                        successCount++;
                    } catch (SQLException e) {
                        if (e.getMessage().contains("Duplicate entry")) {
                            skipCount++;
                        } else {
                            System.err.println("✗ Error executing statement: " + e.getMessage());
                        }
                    }
                }
            }

            System.out.println("\n✓ Data seeding completed!");
            System.out.println("  - Statements executed: " + successCount);
            if (skipCount > 0) {
                System.out.println("  - Duplicates skipped: " + skipCount);
            }

            // Print summary
            try (Statement stmt = conn.createStatement();
                    var rs = stmt.executeQuery("SELECT COUNT(*) as total FROM orders WHERE YEAR(created_at) = 2026")) {
                if (rs.next()) {
                    System.out.println("\n✓ Total orders for 2026: " + rs.getInt("total"));
                }
            }

            try (Statement stmt = conn.createStatement();
                    var rs = stmt.executeQuery(
                            "SELECT SUM(oi.quantity) as total FROM order_items oi " +
                                    "JOIN orders o ON oi.order_id = o.id WHERE YEAR(o.created_at) = 2026")) {
                if (rs.next()) {
                    System.out.println("✓ Total units sold 2026: " + rs.getInt("total"));
                }
            }

            try (Statement stmt = conn.createStatement();
                    var rs = stmt.executeQuery(
                            "SELECT SUM(total_price) as revenue FROM orders " +
                                    "WHERE status_payment = 'PAID' AND YEAR(created_at) = 2026")) {
                if (rs.next()) {
                    double revenue = rs.getDouble("revenue");
                    System.out.println("✓ Total revenue 2026: Rp " + String.format("%,.0f", revenue));
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("✗ Error reading SQL file: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=====================================");
    }
}
