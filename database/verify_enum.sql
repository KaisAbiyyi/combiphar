-- Script untuk memverifikasi apakah ENUM sudah diupdate
USE combiphar_db;

-- Cek struktur tabel items
SHOW COLUMNS FROM items WHERE Field = 'eligibility_status';

-- Output yang diharapkan:
-- eligibility_status | enum('ELIGIBLE','NEEDS_QC','NEEDS_REPAIR','NOT_ELIGIBLE') | NO | | ELIGIBLE |
