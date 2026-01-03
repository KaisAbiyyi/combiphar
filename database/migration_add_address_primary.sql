-- ================================================================
-- MIGRATION: Add is_primary column to addresses table
-- ================================================================
-- Date: 2026-01-02
-- Description: Adds is_primary column to addresses table for 
--              managing primary address per user
-- ================================================================

-- Add is_primary column (MySQL compatible)
-- This will fail if column already exists, which is acceptable for our migration strategy
ALTER TABLE addresses ADD COLUMN is_primary BOOLEAN DEFAULT FALSE;

-- Add index for is_primary column
-- Note: This may fail on re-run if index already exists, which is acceptable
CREATE INDEX idx_is_primary ON addresses(is_primary);
