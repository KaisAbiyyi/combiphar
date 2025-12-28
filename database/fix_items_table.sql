-- Complete Fix Migration: Add missing columns and update ENUM values
-- Date: 2025-12-28
-- Jalankan script ini di MySQL untuk memperbaiki semua masalah

USE combiphar_db;

-- 1. Tambahkan kolom image_url jika belum ada
-- Cek dulu apakah kolom ada
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'combiphar_db' 
    AND TABLE_NAME = 'items' 
    AND COLUMN_NAME = 'image_url'
);

-- Jika tidak ada, tambahkan
SET @add_column = IF(@column_exists = 0, 
    'ALTER TABLE items ADD COLUMN image_url VARCHAR(255) DEFAULT NULL AFTER description', 
    'SELECT "Column image_url already exists"');
PREPARE stmt FROM @add_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Update ENUM untuk eligibility_status agar mendukung NEEDS_QC
ALTER TABLE items 
MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') DEFAULT 'ELIGIBLE';

-- 3. Verifikasi perubahan
SELECT 'Migration completed successfully!' AS status;
SHOW COLUMNS FROM items;
