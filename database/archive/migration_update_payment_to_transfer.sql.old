-- ========================================
-- Migration: Update Payment System to Transfer Method
-- Date: 2026-01-01
-- Description: Update payment system to support only transfer payment method with multiple bank options
-- Idempotent: safe to run multiple times
-- ========================================

-- ========================================
-- STEP 0: Drop check constraints if they exist
-- ========================================

SET @current_db = DATABASE();

-- Drop check constraint on orders table (if exists)
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'orders'
    AND CONSTRAINT_NAME = 'chk_orders_payment_method'
);

SET @drop_constraint = IF(@constraint_exists > 0,
    'ALTER TABLE orders DROP CONSTRAINT chk_orders_payment_method',
    'SELECT "Constraint chk_orders_payment_method does not exist" AS info');

PREPARE stmt FROM @drop_constraint;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop check constraint on payments table (if exists)
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'payments'
    AND CONSTRAINT_NAME = 'chk_payments_type'
);

SET @drop_constraint = IF(@constraint_exists > 0,
    'ALTER TABLE payments DROP CONSTRAINT chk_payments_type',
    'SELECT "Constraint chk_payments_type does not exist" AS info');

PREPARE stmt FROM @drop_constraint;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop check constraint on orders status_order (if exists)
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'orders'
    AND CONSTRAINT_NAME = 'chk_orders_status_order'
);

SET @drop_constraint = IF(@constraint_exists > 0,
    'ALTER TABLE orders DROP CONSTRAINT chk_orders_status_order',
    'SELECT "Constraint chk_orders_status_order does not exist" AS info');

PREPARE stmt FROM @drop_constraint;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- STEP 1: Update orders table - change payment_method to 'TRANSFER' only
-- ========================================

-- Update payment_method ENUM to only support 'TRANSFER'
ALTER TABLE orders
MODIFY COLUMN payment_method ENUM('TRANSFER') NOT NULL;

-- Update existing records with 'QRIS' or 'CASH' to 'TRANSFER'
UPDATE orders
SET payment_method = 'TRANSFER'
WHERE payment_method IN ('QRIS', 'CASH');

SELECT 'Updated orders table payment_method to TRANSFER' AS info;

-- ========================================
-- STEP 2: Add bank column to payments table
-- ========================================

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'payments'
    AND COLUMN_NAME = 'bank'
);

SET @add_column = IF(@column_exists = 0,
    'ALTER TABLE payments ADD COLUMN bank ENUM("BCA", "MANDIRI", "BRI") NULL AFTER type',
    'SELECT "Column bank already exists" AS info');

PREPARE stmt FROM @add_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- STEP 3: Add proof column to payments table
-- ========================================

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'payments'
    AND COLUMN_NAME = 'proof'
);

SET @add_column = IF(@column_exists = 0,
    'ALTER TABLE payments ADD COLUMN proof VARCHAR(255) NULL AFTER bank',
    'SELECT "Column proof already exists" AS info');

PREPARE stmt FROM @add_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- STEP 4: Update payments type column to 'TRANSFER' only
-- ========================================

ALTER TABLE payments
MODIFY COLUMN type ENUM('TRANSFER') NOT NULL;

-- Update existing records with 'QRIS' or 'CASH' to 'TRANSFER'
UPDATE payments
SET type = 'TRANSFER'
WHERE type IN ('QRIS', 'CASH');

SELECT 'Updated payments table type to TRANSFER' AS info;

-- ========================================
-- STEP 5: Remove qris_ref column if it exists
-- ========================================

SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'payments'
    AND COLUMN_NAME = 'qris_ref'
);

SET @drop_column = IF(@column_exists > 0,
    'ALTER TABLE payments DROP COLUMN qris_ref',
    'SELECT "Column qris_ref does not exist" AS info');

PREPARE stmt FROM @drop_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- STEP 6: Create index on bank column (if not exists)
-- ========================================

SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'payments'
    AND INDEX_NAME = 'idx_bank'
);

SET @create_index = IF(@index_exists = 0,
    'CREATE INDEX idx_bank ON payments(bank)',
    'SELECT "Index idx_bank already exists" AS info');

PREPARE stmt FROM @create_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- STEP 7: Create index on proof column (if not exists)
-- ========================================

SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = @current_db
    AND TABLE_NAME = 'payments'
    AND INDEX_NAME = 'idx_proof'
);

SET @create_index = IF(@index_exists = 0,
    'CREATE INDEX idx_proof ON payments(proof)',
    'SELECT "Index idx_proof already exists" AS info');

PREPARE stmt FROM @create_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- STEP 8: Verify migration results
-- ========================================

SELECT '========================================' AS '';
SELECT 'PAYMENT TRANSFER MIGRATION COMPLETED!' AS status;
SELECT '========================================' AS '';

-- Show orders table payment_method column
SELECT 'Orders table - payment_method column:' AS info;
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @current_db
AND TABLE_NAME = 'orders'
AND COLUMN_NAME = 'payment_method';

-- Show payments table structure
SELECT 'Payments table structure:' AS info;
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @current_db
AND TABLE_NAME = 'payments'
AND COLUMN_NAME IN ('type', 'bank', 'proof');

-- Show indexes on payments table
SELECT 'Indexes on payments table:' AS info;
SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = @current_db
AND TABLE_NAME = 'payments'
AND INDEX_NAME IN ('idx_bank', 'idx_proof')
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

SELECT '========================================' AS '';
SELECT 'Migration successful! Payment system updated to transfer method.' AS message;
SELECT '========================================' AS '';

-- ========================================
-- Migration Summary
-- ========================================
-- Changes made:
-- 0. Dropped check constraints for payment_method, payments type, and status_order
-- 1. Updated orders.payment_method ENUM to 'TRANSFER' only
-- 2. Added payments.bank column (ENUM: BCA, MANDIRI, BRI)
-- 3. Added payments.proof column (VARCHAR(255) for transfer proof image)
-- 4. Updated payments.type ENUM to 'TRANSFER' only
-- 5. Removed payments.qris_ref column (QRIS no longer supported)
-- 6. Created indexes on bank and proof columns for performance
-- 7. Migration is idempotent - safe to run multiple times
-- ========================================
