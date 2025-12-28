-- Seed dummy data untuk testing NEEDS_QC status
-- Date: 2025-12-28
-- Jalankan setelah fix_items_table.sql

USE combiphar_db;

-- Ambil category_id yang ada
SET @cat_id = (SELECT id FROM categories LIMIT 1);

-- Insert dummy items dengan status NEEDS_QC
INSERT INTO items (id, category_id, name, `condition`, description, image_url, price, stock, eligibility_status, is_published) VALUES
('qc-item-001', @cat_id, 'Lemari Arsip Bekas', 'USED_GOOD', 'Lemari arsip 4 laci, perlu pengecekan kunci', NULL, 850000, 5, 'NEEDS_QC', false),
('qc-item-002', @cat_id, 'Kursi Putar Ergonomis', 'USED_FAIR', 'Kursi kantor bekas, perlu cek roda dan bantalan', NULL, 450000, 8, 'NEEDS_QC', false),
('qc-item-003', @cat_id, 'Monitor LCD 24 inch', 'USED_GOOD', 'Monitor bekas, perlu tes display dan kabel', NULL, 1200000, 3, 'NEEDS_QC', false),
('qc-item-004', @cat_id, 'Meja Kerja L-Shape', 'NEW', 'Meja baru dari gudang, perlu cek kelengkapan', NULL, 2500000, 2, 'NEEDS_QC', false),
('qc-item-005', @cat_id, 'Rak Buku Besi 5 Tingkat', 'USED_FAIR', 'Rak bekas, perlu cek karat dan stabilitas', NULL, 650000, 4, 'NEEDS_QC', false);

-- Update beberapa item existing menjadi NEEDS_QC jika ada
UPDATE items SET eligibility_status = 'NEEDS_QC' WHERE id IN (
    SELECT id FROM (SELECT id FROM items WHERE eligibility_status = 'ELIGIBLE' LIMIT 2) AS temp
);

SELECT 'Dummy data dengan NEEDS_QC berhasil ditambahkan!' AS status;
SELECT COUNT(*) AS total_needs_qc FROM items WHERE eligibility_status = 'NEEDS_QC';
