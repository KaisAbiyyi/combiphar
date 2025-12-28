-- ========================================
-- MIGRATION FILE - Update Database Structure
-- ========================================
-- File: migrations.sql
-- Date: 2025-12-28
-- Description: Pembaruan database untuk mendukung fitur QC Pipeline dan upload gambar produk
-- 
-- INSTRUKSI:
-- 1. Backup database terlebih dahulu
-- 2. Jalankan file ini setelah schema.sql
-- 3. File ini aman dijalankan berulang kali (idempotent)
-- ========================================

USE combiphar_db;

-- ========================================
-- STEP 1: Hapus CHECK Constraint yang Membatasi
-- ========================================
-- Constraint ini mencegah penggunaan status NEEDS_QC
-- Jika tidak ada constraint, query ini akan diabaikan

SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = 'combiphar_db' 
    AND TABLE_NAME = 'items' 
    AND CONSTRAINT_NAME = 'chk_items_eligibility'
);

-- Drop constraint jika ada
SET @drop_constraint = IF(@constraint_exists > 0,
    'ALTER TABLE items DROP CONSTRAINT chk_items_eligibility',
    'SELECT "No constraint to drop" AS info');

PREPARE stmt FROM @drop_constraint;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ========================================
-- STEP 2: Tambah Kolom image_url untuk Upload Gambar
-- ========================================
-- Menambahkan kolom untuk menyimpan path gambar produk

SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'combiphar_db' 
    AND TABLE_NAME = 'items' 
    AND COLUMN_NAME = 'image_url'
);

SET @add_column = IF(@column_exists = 0,
    'ALTER TABLE items ADD COLUMN image_url VARCHAR(255) DEFAULT NULL AFTER description',
    'SELECT "Column image_url already exists" AS info');

PREPARE stmt FROM @add_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- ========================================
-- STEP 3: Update ENUM untuk Mendukung NEEDS_QC
-- ========================================
-- Menambahkan status NEEDS_QC untuk Quality Control Pipeline

ALTER TABLE items 
MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') 
DEFAULT 'ELIGIBLE';


-- ========================================
-- STEP 4: Seed Data Dummy untuk Testing QC Pipeline
-- ========================================
-- Menambahkan beberapa item dengan status NEEDS_QC untuk testing

-- Ambil category_id yang ada
SET @cat_id = (SELECT id FROM categories LIMIT 1);

-- Hanya insert jika belum ada data QC
INSERT IGNORE INTO items (id, category_id, name, `condition`, description, image_url, price, stock, eligibility_status, is_published) 
SELECT * FROM (
    SELECT 'qc-demo-001' AS id, @cat_id AS category_id, 'Lemari Besi Bekas Gudang' AS name, 'USED_GOOD' AS `condition`, 
           'Lemari besi dari gudang cabang Bekasi, perlu pengecekan kunci dan kondisi cat.' AS description, 
           NULL AS image_url, 1200000 AS price, 5 AS stock, 'NEEDS_QC' AS eligibility_status, FALSE AS is_published
    UNION ALL
    SELECT 'qc-demo-002', @cat_id, 'Kursi Direktur Premium', 'USED_GOOD',
           'Kursi kulit asli dari ruang direktur, perlu cek mekanisme recline dan roda.',
           NULL, 2800000, 2, 'NEEDS_QC', FALSE
    UNION ALL
    SELECT 'qc-demo-003', @cat_id, 'Proyektor BenQ 3500 Lumen', 'USED_FAIR',
           'Proyektor bekas ruang meeting lantai 3, perlu tes lampu dan kualitas lensa.',
           NULL, 3500000, 1, 'NEEDS_QC', FALSE
    UNION ALL
    SELECT 'qc-demo-004', @cat_id, 'Scanner Dokumen A3', 'USED_GOOD',
           'Scanner flatbed ukuran A3 untuk dokumen besar, perlu kalibrasi warna dan sensor.',
           NULL, 4200000, 3, 'NEEDS_QC', FALSE
    UNION ALL
    SELECT 'qc-demo-005', @cat_id, 'UPS Rack Mount 3KVA', 'USED_GOOD',
           'UPS untuk server rack data center, perlu cek kondisi baterai dan output voltage.',
           NULL, 5500000, 2, 'NEEDS_QC', FALSE
    UNION ALL
    SELECT 'qc-demo-006', @cat_id, 'Monitor Gaming 27 inch', 'USED_GOOD',
           'Monitor 144Hz dari ruang desain, perlu pengecekan dead pixel dan kabel.',
           NULL, 2100000, 4, 'NEEDS_QC', FALSE
    UNION ALL
    SELECT 'qc-demo-007', @cat_id, 'Meja Rapat Lipat', 'USED_FAIR',
           'Meja lipat kapasitas 6 orang, perlu cek engsel dan stabilitas kaki.',
           NULL, 1500000, 3, 'NEEDS_QC', FALSE
) AS tmp;


-- ========================================
-- STEP 5: Verifikasi Hasil Migration
-- ========================================

SELECT '========================================' AS '';
SELECT 'MIGRATION COMPLETED SUCCESSFULLY!' AS status;
SELECT '========================================' AS '';

-- Tampilkan struktur tabel items
SELECT 'Struktur tabel items:' AS info;
DESCRIBE items;

-- Hitung total items per status
SELECT 'Jumlah items per status kelayakan:' AS info;
SELECT 
    eligibility_status,
    COUNT(*) AS total,
    SUM(stock) AS total_stock
FROM items 
GROUP BY eligibility_status;

-- Tampilkan items NEEDS_QC untuk QC Pipeline
SELECT 'Items dengan status NEEDS_QC (untuk Pipeline QC):' AS info;
SELECT id, name, `condition`, stock, price 
FROM items 
WHERE eligibility_status = 'NEEDS_QC'
LIMIT 10;

SELECT '========================================' AS '';
SELECT 'Migration berhasil! Aplikasi siap digunakan.' AS message;
SELECT '========================================' AS '';
