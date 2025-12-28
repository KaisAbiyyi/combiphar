-- Migration: Add NEEDS_QC to eligibility_status ENUM
-- Date: 2025-12-27
-- Description: Menambahkan status NEEDS_QC ke dalam eligibility_status untuk mendukung Quality Control pipeline

USE combiphar_db;

-- Alter the items table to update the ENUM values
ALTER TABLE items 
MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') DEFAULT 'ELIGIBLE';

-- Update schema.sql juga perlu diupdate untuk future reference
