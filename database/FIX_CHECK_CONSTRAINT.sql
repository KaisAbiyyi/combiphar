-- FIX CHECK CONSTRAINT untuk eligibility_status
-- Jalankan query ini di MySQL!

USE combiphar_db;

-- 1. Hapus CHECK constraint yang ada (jika ada)
-- Cari nama constraint dulu
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'combiphar_db' 
AND TABLE_NAME = 'items' 
AND CONSTRAINT_TYPE = 'CHECK';

-- 2. Hapus constraint (ganti nama jika berbeda)
ALTER TABLE items DROP CONSTRAINT chk_items_eligibility;

-- ATAU jika tidak bisa dihapus, drop dan recreate constraint:
-- ALTER TABLE items DROP CHECK chk_items_eligibility;

-- 3. Update ENUM untuk eligibility_status
ALTER TABLE items 
MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') DEFAULT 'ELIGIBLE';

-- 4. Tambah kolom image_url jika belum ada
-- ALTER TABLE items ADD COLUMN image_url VARCHAR(255) DEFAULT NULL AFTER description;

-- 5. Verifikasi
DESCRIBE items;

-- 6. Test insert dengan NEEDS_QC
INSERT INTO items (id, category_id, name, `condition`, description, price, stock, eligibility_status, is_published) 
VALUES ('test-qc-001', (SELECT id FROM categories LIMIT 1), 'Test QC Item', 'NEW', 'Test description', 100000, 10, 'NEEDS_QC', false);

-- 7. Hapus data test
DELETE FROM items WHERE id = 'test-qc-001';

SELECT 'Fix completed!' AS status;
