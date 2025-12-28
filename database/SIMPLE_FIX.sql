-- SIMPLE FIX: Jalankan query ini satu per satu di MySQL
-- Date: 2025-12-28

-- LANGKAH 1: Pilih database
USE combiphar_db;

-- LANGKAH 2: Tambah kolom image_url (abaikan error jika sudah ada)
ALTER TABLE items ADD COLUMN image_url VARCHAR(255) DEFAULT NULL AFTER description;

-- LANGKAH 3: Update ENUM untuk eligibility_status
ALTER TABLE items MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') DEFAULT 'ELIGIBLE';

-- LANGKAH 4: Verifikasi struktur tabel
DESCRIBE items;
