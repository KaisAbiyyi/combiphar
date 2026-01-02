package com.combiphar.core.seeder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Seeder for creating test users (customers, admin).
 * Defensive programming: Can be run multiple times without duplicates.
 */
public class UserSeeder {

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   User Seeder");
        System.out.println("=====================================\n");

        try {
            // Initialize database connection
            DatabaseConfig.getConnection().close();
            System.out.println("✓ Database connection successful\n");

            // Seed admin
            seedUser(
                "admin@combiphar.com",
                "Admin123456",
                "Admin Combiphar",
                "ADMIN",
                "021-1234567",
                "Jakarta, Indonesia",
                "ACTIVE"
            );

            // Seed test customers
            seedUser(
                "customer1@test.com",
                "Customer123",
                "Budi Santoso",
                "CUSTOMER",
                "081234567890",
                "Jl. Sudirman No. 1, Jakarta",
                "ACTIVE"
            );

            seedUser(
                "customer2@test.com",
                "Customer123",
                "Siti Rahayu",
                "CUSTOMER",
                "081234567891",
                "Jl. Thamrin No. 2, Jakarta",
                "ACTIVE"
            );

            seedUser(
                "customer3@test.com",
                "Customer123",
                "Ahmad Wijaya",
                "CUSTOMER",
                "081234567892",
                "Jl. Gatot Subroto No. 3, Jakarta",
                "ACTIVE"
            );

            System.out.println("\n✓ All users seeded successfully!");
            System.out.println("\nLogin Credentials:");
            System.out.println("  Admin   : admin@combiphar.com / Admin123456");
            System.out.println("  Customer: customer1@test.com / Customer123");
            System.exit(0);

        } catch (Exception e) {
            System.err.println("✗ Error during seeding: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void seedUser(String email, String password, String name, String role,
            String phone, String address, String status) throws SQLException {

        // Check if user already exists
        if (userExists(email)) {
            System.out.println("⚠ User already exists: " + email + " (" + role + ")");
            return;
        }

        String sql = "INSERT INTO users (id, name, email, password, role, phone, address, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String userId = UUID.randomUUID().toString();
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, userId);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            stmt.setString(5, role);
            stmt.setString(6, phone);
            stmt.setString(7, address);
            stmt.setString(8, status);

            stmt.executeUpdate();
            System.out.println("✓ User created: " + email + " (" + role + ")");
        }
    }

    private static boolean userExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
