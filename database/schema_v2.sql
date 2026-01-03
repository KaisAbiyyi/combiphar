-- ================================================================
-- COMBIPHAR DATABASE SCHEMA V2
-- ================================================================
-- Version: 2.0
-- Date: 2026-01-02
-- Description: Complete database schema with all migrations combined
-- Features:
--   - User management with roles
--   - Category management with status
--   - Item/Product management with QC pipeline
--   - Shopping cart (persistent)
--   - Order management with addresses
--   - Payment system (Transfer method with multiple banks)
--   - Shipment tracking
--
-- Usage:
--   mysql -u root -p < database/schema_v2.sql
-- ================================================================

-- Create database
CREATE DATABASE IF NOT EXISTS combiphar_db;
USE combiphar_db;

-- ================================================================
-- TABLES: USER MANAGEMENT
-- ================================================================

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLES: CATEGORY MANAGEMENT
-- ================================================================

CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status ENUM('AKTIF', 'REVIEW', 'DRAFT') DEFAULT 'AKTIF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLES: ITEM/PRODUCT MANAGEMENT
-- ================================================================

CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36),
    name VARCHAR(150) NOT NULL,
    `condition` ENUM('NEW', 'USED_GOOD', 'USED_FAIR', 'DAMAGED') NOT NULL,
    description TEXT,
    image_url VARCHAR(255) DEFAULT NULL,
    price DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') DEFAULT 'ELIGIBLE',
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_category_id (category_id),
    INDEX idx_eligibility_status (eligibility_status),
    INDEX idx_is_published (is_published),
    INDEX idx_condition (`condition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLES: SHOPPING CART
-- ================================================================

CREATE TABLE IF NOT EXISTS carts (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(12,2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cart_id (cart_id),
    INDEX idx_item_id (item_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLES: ADDRESS MANAGEMENT
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
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_province (province),
    INDEX idx_is_primary (is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLES: ORDER MANAGEMENT
-- ================================================================

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    address_id VARCHAR(36) NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    total_price DECIMAL(12,2) NOT NULL,
    payment_method ENUM('TRANSFER') NOT NULL,
    pickup_method ENUM('PICKUP', 'DELIVERY') NOT NULL,
    status_payment ENUM('PENDING', 'PAID', 'FAILED') DEFAULT 'PENDING',
    status_order ENUM('NEW', 'PROCESSING', 'READY', 'COMPLETED', 'CANCELLED') DEFAULT 'NEW',
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_address_id (address_id),
    INDEX idx_order_number (order_number),
    INDEX idx_status_payment (status_payment),
    INDEX idx_status_order (status_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    item_id VARCHAR(36),
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    note TEXT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE SET NULL,
    INDEX idx_order_id (order_id),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLES: PAYMENT MANAGEMENT (TRANSFER METHOD)
-- ================================================================

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    type ENUM('TRANSFER') NOT NULL,
    bank ENUM('BCA', 'MANDIRI', 'BRI') NULL,
    proof VARCHAR(255) NULL,
    amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_bank (bank),
    INDEX idx_proof (proof),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLES: SHIPMENT MANAGEMENT
-- ================================================================

CREATE TABLE IF NOT EXISTS shipments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    address_id VARCHAR(36) NULL,
    courier_name VARCHAR(100),
    tracking_number VARCHAR(100),
    shipment_status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'RECEIVED') DEFAULT 'PENDING',
    shipped_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES addresses(id) ON DELETE SET NULL,
    INDEX idx_order_id (order_id),
    INDEX idx_address_id (address_id),
    INDEX idx_shipment_status (shipment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- VERIFICATION
-- ================================================================

SELECT '========================================' AS '';
SELECT 'SCHEMA V2 CREATED SUCCESSFULLY!' AS status;
SELECT '========================================' AS '';

-- Show all tables
SELECT 'All tables in database:' AS info;
SHOW TABLES;

-- Show table structures
SELECT 'Structure of users table:' AS info;
DESCRIBE users;

SELECT 'Structure of categories table:' AS info;
DESCRIBE categories;

SELECT 'Structure of items table:' AS info;
DESCRIBE items;

SELECT 'Structure of carts table:' AS info;
DESCRIBE carts;

SELECT 'Structure of cart_items table:' AS info;
DESCRIBE cart_items;

SELECT 'Structure of addresses table:' AS info;
DESCRIBE addresses;

SELECT 'Structure of orders table:' AS info;
DESCRIBE orders;

SELECT 'Structure of order_items table:' AS info;
DESCRIBE order_items;

SELECT 'Structure of payments table:' AS info;
DESCRIBE payments;

SELECT 'Structure of shipments table:' AS info;
DESCRIBE shipments;

SELECT '========================================' AS '';
SELECT 'Schema V2 setup complete!' AS message;
SELECT '========================================' AS '';
