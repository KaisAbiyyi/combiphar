# Database Setup & Migration

## File yang Ada

1. **schema.sql** - File asli untuk struktur database dasar
2. **migrations.sql** - File pembaruan database untuk fitur baru
3. **add_image_to_items.sql** - Migration lama (optional)
4. **add_status_to_categories.sql** - Migration lama (optional)

## Cara Setup Database

### Instalasi Baru (Fresh Install)

Jika Anda baru setup database pertama kali:

```bash
# 1. Jalankan schema.sql untuk membuat struktur dasar
mysql -u root -p < database/schema.sql

# 2. Jalankan migrations.sql untuk update dan seed data
mysql -u root -p < database/migrations.sql
```

### Update Database yang Sudah Ada

Jika database sudah ada dan hanya perlu update:

```bash
# Jalankan migrations.sql saja
mysql -u root -p < database/migrations.sql
```

## Isi migrations.sql

File `migrations.sql` berisi:

1. **Drop CHECK Constraint** - Menghapus constraint yang membatasi `NEEDS_QC`
2. **Tambah Kolom `image_url`** - Untuk fitur upload gambar produk
3. **Update ENUM `eligibility_status`** - Menambahkan nilai `NEEDS_QC` dan `NOT_ELIGIBLE`
4. **Seed Data Dummy** - 7 items dengan status `NEEDS_QC` untuk testing Pipeline QC

## Fitur yang Ditambahkan

### 1. Status Kelayakan NEEDS_QC

Mendukung status baru untuk Quality Control Pipeline:

- `ELIGIBLE` - Layak Jual
- `NEEDS_QC` - Perlu QC ✨ (BARU)
- `NEEDS_REPAIR` - Label Ulang
- `NOT_ELIGIBLE` - Tidak Layak ✨ (BARU)

### 2. Upload Gambar Produk

Kolom `image_url` untuk menyimpan path gambar produk yang diupload.

### 3. Data Dummy untuk Testing

7 items otomatis ditambahkan dengan status `NEEDS_QC` untuk menguji:

- Card "Pipeline QC Hari Ini"
- Card "Perlu QC" count
- Filter status kelayakan

## Troubleshooting

### Error: "Duplicate column name 'image_url'"

Abaikan error ini. Kolom sudah ada dari migration sebelumnya.

### Error: "Check constraint 'chk_items_eligibility' is violated"

Jalankan migrations.sql untuk menghapus constraint lama.

### Tidak muncul data di Pipeline QC

1. Pastikan migrations.sql sudah dijalankan
2. Cek di database: `SELECT * FROM items WHERE eligibility_status = 'NEEDS_QC'`
3. Restart aplikasi setelah menjalankan migration

## Verifikasi

Setelah menjalankan migrations.sql, Anda bisa verifikasi dengan:

```sql
-- Cek struktur tabel
DESCRIBE items;

-- Cek jumlah items per status
SELECT eligibility_status, COUNT(*)
FROM items
GROUP BY eligibility_status;

-- Lihat items NEEDS_QC
SELECT id, name, stock, price
FROM items
WHERE eligibility_status = 'NEEDS_QC';
```

## Catatan Penting

- ⚠️ **Backup database sebelum menjalankan migration**
- ✅ File migrations.sql aman dijalankan berulang kali (idempotent)
- ✅ Tidak akan menghapus data yang sudah ada
- ✅ Hanya menambahkan kolom dan data baru jika belum ada
