package com.combiphar.core.seeder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;

/**
 * Seeder for creating dummy product data
 */
public class ProductSeeder {

    public static void main(String[] args) {
        try {
            // Get categories first
            List<String> categoryIds = getCategoryIds();

            if (categoryIds.isEmpty()) {
                System.out.println("No categories found. Please run CategorySeeder first.");
                return;
            }

            // Clear existing items (optional - comment out if you want to keep existing
            // data)
            clearItems();

            // Create dummy products
            createDummyProducts(categoryIds);

            System.out.println("✅ Product seeding completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error seeding products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> getCategoryIds() throws SQLException {
        String sql = "SELECT id FROM categories WHERE status = 'AKTIF' LIMIT 5";
        List<String> ids = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        }
        return ids;
    }

    private static void clearItems() throws SQLException {
        String sql = "DELETE FROM items";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            int deleted = stmt.executeUpdate();
            System.out.println("🗑️  Cleared " + deleted + " existing items");
        }
    }

    private static void createDummyProducts(List<String> categoryIds) throws SQLException {
        // Define dummy products with varied data
        Object[][] products = {
                // {name, condition, description, imageUrl, price, stock, eligibilityStatus,
                // isPublished}
                { "Lemari Arsip Besi Tinggi", "NEW",
                        "Lemari arsip besi 4 pintu dengan kunci ganda. Kapasitas besar untuk penyimpanan dokumen.",
                        "/images/products/cabinet.jpg", 2500000, 15, "ELIGIBLE", true },

                { "Meja Kerja Executive", "NEW",
                        "Meja kerja kayu mahogani dengan laci samping. Ukuran 160x80cm, finishing glossy.",
                        "/images/products/desk.jpg", 3500000, 8, "ELIGIBLE", true },

                { "Kursi Kantor Ergonomis", "NEW",
                        "Kursi kantor dengan sandaran punggung tinggi, arm rest adjustable, dan roda putar.",
                        "/images/products/chair.jpg", 1800000, 25, "ELIGIBLE", true },

                { "Komputer Desktop Set", "USED_GOOD",
                        "PC i5 Gen 8, RAM 8GB, SSD 256GB + HDD 1TB, monitor 22 inch. Kondisi terawat.",
                        "/images/products/computer.jpg", 6500000, 5, "ELIGIBLE", true },

                { "Printer Multifungsi", "NEW",
                        "Printer inkjet all-in-one print, scan, copy. Cocok untuk kebutuhan kantor.",
                        "/images/products/printer.jpg", 2200000, 12, "ELIGIBLE", true },

                { "Rak Buku Kayu Jati", "USED_GOOD",
                        "Rak buku 5 tingkat kayu jati solid. Dimensi 180x80x30cm. Kondisi baik.",
                        "/images/products/bookshelf.jpg", 1500000, 7, "ELIGIBLE", false },

                { "Meja Meeting Oval", "NEW", "Meja meeting kapasitas 8 orang, finishing melamine. Ukuran 240x120cm.",
                        "/images/products/meeting-table.jpg", 4800000, 3, "ELIGIBLE", true },

                { "Filling Cabinet 4 Laci", "USED_FAIR",
                        "Filing cabinet besi 4 laci dengan rel sliding. Ada beberapa goresan minor.",
                        "/images/products/filing-cabinet.jpg", 850000, 10, "ELIGIBLE", false },

                { "Whiteboard Magnetik", "NEW", "Whiteboard ukuran 120x90cm dengan frame aluminium dan marker tray.",
                        "/images/products/whiteboard.jpg", 650000, 20, "ELIGIBLE", true },

                { "Sofa Kantor 3 Dudukan", "USED_GOOD",
                        "Sofa kantor rangka kayu dengan cushion kulit sintetis. Kondisi bersih.",
                        "/images/products/sofa.jpg", 3200000, 4, "ELIGIBLE", true },

                { "Monitor LCD 24 inch", "USED_GOOD", "Monitor LED full HD 1920x1080, HDMI + VGA port. Masih mulus.",
                        "/images/products/monitor.jpg", 1400000, 8, "ELIGIBLE", true },

                { "AC Split 1 PK", "NEW", "AC split inverter hemat energi, low watt. Garansi resmi 3 tahun.",
                        "/images/products/ac.jpg", 3800000, 6, "NEEDS_REPAIR", false },

                { "Kursi Tunggu 3 Seater", "USED_FAIR",
                        "Kursi tunggu rangka besi dengan jok busa. Perlu penggantian kain pelapis.",
                        "/images/products/waiting-chair.jpg", 950000, 2, "NEEDS_REPAIR", false },

                { "Meja Komputer Minimalis", "NEW",
                        "Meja komputer compact dengan keyboard tray dan rak CPU. Ukuran 100x60cm.",
                        "/images/products/computer-desk.jpg", 850000, 18, "ELIGIBLE", true },

                { "Lemari Arsip Sliding Door", "NEW",
                        "Lemari arsip pintu sliding dengan 3 rak adjustable. Material particle board.",
                        "/images/products/sliding-cabinet.jpg", 1900000, 9, "ELIGIBLE", true },

                // NEEDS_QC Items - untuk Pipeline QC Hari Ini
                { "Lemari Besi Bekas Gudang", "USED_GOOD",
                        "Lemari besi dari gudang cabang, perlu pengecekan kunci dan kondisi cat.",
                        null, 1200000, 5, "NEEDS_QC", false },

                { "Kursi Direktur Premium", "USED_GOOD",
                        "Kursi kulit asli dari ruang direktur, perlu cek mekanisme recline.",
                        null, 2800000, 2, "NEEDS_QC", false },

                { "Proyektor BenQ 3500 Lumen", "USED_FAIR", "Proyektor bekas ruang meeting, perlu tes lampu dan lensa.",
                        null, 3500000, 1, "NEEDS_QC", false },

                { "Scanner Dokumen A3", "USED_GOOD", "Scanner flatbed A3 untuk dokumen besar, perlu kalibrasi warna.",
                        null, 4200000, 3, "NEEDS_QC", false },

                { "UPS Rack Mount 3KVA", "USED_GOOD", "UPS untuk server rack, perlu cek baterai dan output.",
                        null, 5500000, 2, "NEEDS_QC", false }
        };

        String sql = "INSERT INTO items (id, category_id, name, `condition`, description, image_url, price, stock, eligibility_status, is_published) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            int count = 0;
            for (Object[] product : products) {
                String categoryId = categoryIds.get(count % categoryIds.size()); // Rotate through categories

                stmt.setString(1, UUID.randomUUID().toString());
                stmt.setString(2, categoryId);
                stmt.setString(3, (String) product[0]); // name
                stmt.setString(4, (String) product[1]); // condition
                stmt.setString(5, (String) product[2]); // description
                stmt.setString(6, (String) product[3]); // imageUrl
                stmt.setBigDecimal(7, new BigDecimal((Integer) product[4])); // price
                stmt.setInt(8, (Integer) product[5]); // stock
                stmt.setString(9, (String) product[6]); // eligibilityStatus
                stmt.setBoolean(10, (Boolean) product[7]); // isPublished

                stmt.executeUpdate();
                count++;
                System.out.println("✓ Created: " + product[0]);
            }

            System.out.println("\n📦 Total products created: " + count);
        }
    }
}
