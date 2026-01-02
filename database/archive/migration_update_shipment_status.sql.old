-- Migration: Update shipment_status enum to include PROCESSING and RECEIVED
-- Date: 2026-01-02

-- Step 1: Alter table to modify ENUM type
ALTER TABLE shipments 
MODIFY COLUMN shipment_status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'RECEIVED') DEFAULT 'PENDING';

-- Step 2: Update any existing 'PACKED' values to 'PROCESSING' (if any)
UPDATE shipments 
SET shipment_status = 'PROCESSING' 
WHERE shipment_status = 'PACKED';
