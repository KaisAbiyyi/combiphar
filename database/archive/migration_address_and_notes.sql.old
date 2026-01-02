-- ================================================================
-- Migration: Address Table and Order Enhancements
-- Date: 2026-01-02
-- Description: 
--   1. Create addresses table
--   2. Add address_id to orders and shipments
--   3. Add note column to order_items
--   4. Update shipment_status enum values
-- Safe to run multiple times (idempotent)
-- ================================================================

-- Use the database
USE combiphar_db;

-- ================================================================
-- 1. CREATE ADDRESSES TABLE
-- ================================================================
CREATE TABLE IF NOT EXISTS addresses (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    subdistrict VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100),
    postal_code VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- ================================================================
-- 2. ADD ADDRESS_ID TO ORDERS TABLE
-- ================================================================
-- Check if column exists before adding
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'combiphar_db' 
    AND TABLE_NAME = 'orders' 
    AND COLUMN_NAME = 'address_id'
);

SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE orders ADD COLUMN address_id VARCHAR(36) NULL AFTER user_id, ADD INDEX idx_address_id (address_id)',
    'SELECT "Column address_id already exists in orders table" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ================================================================
-- 3. ADD ADDRESS_ID TO SHIPMENTS TABLE
-- ================================================================
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'combiphar_db' 
    AND TABLE_NAME = 'shipments' 
    AND COLUMN_NAME = 'address_id'
);

SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE shipments ADD COLUMN address_id VARCHAR(36) NULL AFTER order_id, ADD INDEX idx_shipment_address (address_id)',
    'SELECT "Column address_id already exists in shipments table" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ================================================================
-- 4. ADD NOTE COLUMN TO ORDER_ITEMS
-- ================================================================
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'combiphar_db' 
    AND TABLE_NAME = 'order_items' 
    AND COLUMN_NAME = 'note'
);

SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE order_items ADD COLUMN note TEXT NULL AFTER subtotal',
    'SELECT "Column note already exists in order_items table" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ================================================================
-- 5. UPDATE SHIPMENT_STATUS ENUM
-- ================================================================
-- This will update the enum to include PROCESSING and RECEIVED
-- and ensure the values match the new labels
ALTER TABLE shipments 
MODIFY COLUMN shipment_status ENUM(
    'PENDING',      -- Menunggu Konfirmasi
    'PROCESSING',   -- Diproses
    'SHIPPED',      -- Dalam Perjalanan
    'DELIVERED',    -- Paket Terkirim
    'RECEIVED'      -- Pesanan Selesai
) DEFAULT 'PENDING';

-- Update any old PACKED values to PROCESSING (if any exist)
UPDATE shipments 
SET shipment_status = 'PROCESSING' 
WHERE shipment_status = 'PACKED';

-- ================================================================
-- VERIFICATION QUERIES (Optional - comment out if not needed)
-- ================================================================
SELECT 'Migration completed successfully!' AS status;

-- Show addresses table structure
DESCRIBE addresses;

-- Show updated orders columns
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'combiphar_db' 
AND TABLE_NAME = 'orders' 
AND COLUMN_NAME IN ('address_id');

-- Show updated shipments columns
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'combiphar_db' 
AND TABLE_NAME = 'shipments' 
AND COLUMN_NAME IN ('address_id', 'shipment_status');

-- Show updated order_items columns
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'combiphar_db' 
AND TABLE_NAME = 'order_items' 
AND COLUMN_NAME = 'note';
