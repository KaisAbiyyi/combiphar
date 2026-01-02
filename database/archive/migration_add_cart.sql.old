-- ========================================
-- Migration: Add carts and cart_items tables
-- Date: 2025-12-29
-- Purpose: Persistent shopping cart storage for authenticated users
-- Idempotent: safe to run multiple times
-- ========================================

CREATE DATABASE IF NOT EXISTS combiphar_db;
USE combiphar_db;

-- carts table stores a single cart per user (optional)
CREATE TABLE IF NOT EXISTS carts (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL UNIQUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- cart_items stores the snapshot of items in a cart
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

-- Helpful select to verify
SELECT 'CART MIGRATION: OK' AS info;
SELECT COUNT(*) AS carts_count FROM carts;
SELECT COUNT(*) AS cart_items_count FROM cart_items;

-- End of migration
