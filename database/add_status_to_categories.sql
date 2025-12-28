-- Add status column to categories table
USE combiphar_db;

ALTER TABLE categories 
ADD COLUMN status ENUM('AKTIF', 'REVIEW', 'DRAFT') DEFAULT 'AKTIF' AFTER description;

-- Update existing categories to have 'AKTIF' status
UPDATE categories SET status = 'AKTIF' WHERE status IS NULL;
