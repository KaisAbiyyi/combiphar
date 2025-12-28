-- Migration: Add image_url column to items table
-- Date: 2025-12-28
-- Description: Menambahkan kolom image_url untuk menyimpan path gambar produk

USE combiphar_db;

-- Add image_url column if it doesn't exist
ALTER TABLE items 
ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) DEFAULT NULL AFTER description;
