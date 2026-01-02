package com.combiphar.core.seeder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Seeder for creating test addresses.
 * Defensive programming: Can be run multiple times without duplicates.
 */
public class AddressSeeder {

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   Address Seeder");
        System.out.println("=====================================\n");

        try {
            // Initialize database connection
            DatabaseConfig.getConnection().close();
            System.out.println("✓ Database connection successful\n");

            // Get user IDs
            String customer1Id = getUserIdByEmail("customer1@test.com");
            String customer2Id = getUserIdByEmail("customer2@test.com");
            String customer3Id = getUserIdByEmail("customer3@test.com");

            if (customer1Id == null) {
                System.out.println("⚠ No users found. Please run UserSeeder first.");
                System.exit(1);
            }

            int count = 0;

            // Seed addresses for customer1
            count += seedAddress(customer1Id, "Budi Santoso", "081234567890",
                    "Jl. Sudirman No. 1, Kebayoran Baru", "Kebayoran Baru", "Kebayoran Baru", "Jakarta Selatan",
                    "DKI Jakarta", "12190");
            count += seedAddress(customer1Id, "Budi Santoso", "081234567890",
                    "Ruko Grand Boulevard Block C No. 12", "Kelapa Gading", "Kelapa Gading", "Jakarta Utara",
                    "DKI Jakarta", "14240");

            // Seed addresses for customer2
            count += seedAddress(customer2Id, "Siti Rahayu", "081234567891",
                    "Jl. Thamrin No. 2, RT.1/RW.2", "Menteng", "Menteng", "Jakarta Pusat",
                    "DKI Jakarta", "10350");
            count += seedAddress(customer2Id, "Siti Rahayu", "081234567891",
                    "Apartment Taman Rasuna Tower 5, Unit 12B", "Setiabudi", "Setiabudi", "Jakarta Selatan",
                    "DKI Jakarta", "12960");

            // Seed addresses for customer3
            count += seedAddress(customer3Id, "Ahmad Wijaya", "081234567892",
                    "Jl. Gatot Subroto No. 3, Kuningan", "Setiabudi", "Setiabudi", "Jakarta Selatan",
                    "DKI Jakarta", "12950");
            count += seedAddress(customer3Id, "Ahmad Wijaya", "081234567892",
                    "Graha Kencana Office Tower, Lt. 8", "Kebon Jeruk", "Kebon Jeruk", "Jakarta Barat",
                    "DKI Jakarta", "11530");

            System.out.println("\n✓ Total addresses seeded: " + count);
            System.exit(0);

        } catch (Exception e) {
            System.err.println("✗ Error during seeding: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int seedAddress(String userId, String recipientName, String phone,
            String addressLine, String subdistrict, String district, String city,
            String province, String postalCode) throws SQLException {

        // Check if address already exists (by checking combination of user_id + recipient_name + address)
        if (addressExists(userId, recipientName, addressLine)) {
            System.out.println("⚠ Address already exists for " + recipientName);
            return 0;
        }

        String sql = "INSERT INTO addresses (id, user_id, recipient_name, phone, address, " +
                     "subdistrict, district, city, province, postal_code) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, userId);
            stmt.setString(3, recipientName);
            stmt.setString(4, phone);
            stmt.setString(5, addressLine);
            stmt.setString(6, subdistrict);
            stmt.setString(7, district);
            stmt.setString(8, city);
            stmt.setString(9, province);
            stmt.setString(10, postalCode);

            stmt.executeUpdate();
            System.out.println("✓ Address created: " + recipientName + " - " + city);
            return 1;
        }
    }

    private static boolean addressExists(String userId, String recipientName, String address) throws SQLException {
        String sql = "SELECT COUNT(*) FROM addresses WHERE user_id = ? AND recipient_name = ? AND address = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, recipientName);
            stmt.setString(3, address);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
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
