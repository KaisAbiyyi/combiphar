package com.combiphar.core.seeder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Seeder khusus untuk menambahkan item dengan status NEEDS_QC
 * Jalankan class ini untuk menambahkan data dummy QC Pipeline
 */
public class QCItemSeeder {

    public static void main(String[] args) {
        try {
            System.out.println("🔧 Starting QC Items Seeder...\n");

            // Get a valid category_id
            String categoryId = getFirstCategoryId();

            if (categoryId == null) {
                System.out.println("❌ No categories found. Please run CategorySeeder first.");
                return;
            }

            System.out.println("📁 Using category ID: " + categoryId + "\n");

            // Insert NEEDS_QC items
            insertQCItems(categoryId);

            // Show count
            int count = countNeedsQCItems();
            System.out.println("\n✅ Seeding completed!");
            System.out.println("📊 Total items dengan status NEEDS_QC: " + count);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getFirstCategoryId() throws SQLException {
        String sql = "SELECT id FROM categories LIMIT 1";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("id");
            }
        }
        return null;
    }

    private static void insertQCItems(String categoryId) throws SQLException {
        // Data dummy dengan status NEEDS_QC
        Object[][] items = {
                { "Lemari Besi Bekas Gudang", "USED_GOOD",
                        "Lemari besi dari gudang cabang Bekasi, perlu pengecekan kunci dan kondisi cat.", 1200000, 5 },
                { "Kursi Direktur Premium", "USED_GOOD",
                        "Kursi kulit asli dari ruang direktur, perlu cek mekanisme recline dan roda.", 2800000, 2 },
                { "Proyektor BenQ 3500 Lumen", "USED_FAIR",
                        "Proyektor bekas ruang meeting lantai 3, perlu tes lampu dan kualitas lensa.", 3500000, 1 },
                { "Scanner Dokumen A3", "USED_GOOD",
                        "Scanner flatbed ukuran A3 untuk dokumen besar, perlu kalibrasi warna dan sensor.", 4200000,
                        3 },
                { "UPS Rack Mount 3KVA", "USED_GOOD",
                        "UPS untuk server rack data center, perlu cek kondisi baterai dan output voltage.", 5500000,
                        2 },
                { "Monitor Gaming 27 inch", "USED_GOOD",
                        "Monitor 144Hz dari ruang desain, perlu pengecekan dead pixel dan kabel.", 2100000, 4 },
                { "Meja Rapat Lipat", "USED_FAIR",
                        "Meja lipat kapasitas 6 orang, perlu cek engsel dan stabilitas kaki.", 1500000, 3 }
        };

        String sql = "INSERT INTO items (id, category_id, name, `condition`, description, price, stock, eligibility_status, is_published) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'NEEDS_QC', false)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Object[] item : items) {
                String id = "qc-" + UUID.randomUUID().toString().substring(0, 8);

                stmt.setString(1, id);
                stmt.setString(2, categoryId);
                stmt.setString(3, (String) item[0]); // name
                stmt.setString(4, (String) item[1]); // condition
                stmt.setString(5, (String) item[2]); // description
                stmt.setBigDecimal(6, new BigDecimal((Integer) item[3])); // price
                stmt.setInt(7, (Integer) item[4]); // stock

                try {
                    stmt.executeUpdate();
                    System.out.println("✓ Ditambahkan: " + item[0]);
                } catch (SQLException e) {
                    // Jika gagal (mungkin duplicate), skip saja
                    System.out.println("⚠ Gagal: " + item[0] + " - " + e.getMessage());
                }
            }
        }
    }

    private static int countNeedsQCItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM items WHERE eligibility_status = 'NEEDS_QC'";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
