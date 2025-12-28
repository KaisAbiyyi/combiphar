# Migration Required: Add NEEDS_QC Status

## Masalah
Saat menyimpan produk dengan status kelayakan "Perlu QC" (NEEDS_QC), terjadi error karena database ENUM `eligibility_status` tidak mencakup value tersebut.

## Solusi
Jalankan migration untuk menambahkan NEEDS_QC ke dalam ENUM.

## Cara Menjalankan Migration

### Option 1: Via MySQL Command Line
```bash
mysql -u root -p combiphar_db < database/add_needs_qc_status.sql
```

### Option 2: Via MySQL Workbench
1. Buka MySQL Workbench
2. Connect ke database `combiphar_db`
3. Buka file `database/add_needs_qc_status.sql`
4. Execute query

### Option 3: Via phpMyAdmin (XAMPP)
1. Buka phpMyAdmin (http://localhost/phpmyadmin)
2. Pilih database `combiphar_db`
3. Klik tab "SQL"
4. Copy paste isi dari `database/add_needs_qc_status.sql`
5. Click "Go"

### Option 4: Manual Query
Jalankan query berikut di MySQL:

```sql
USE combiphar_db;

ALTER TABLE items 
MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') DEFAULT 'ELIGIBLE';
```

## Verifikasi
Setelah menjalankan migration, verifikasi dengan:

```sql
SHOW COLUMNS FROM items LIKE 'eligibility_status';
```

Output harus menunjukkan:
```
eligibility_status | enum('ELIGIBLE','NEEDS_QC','NEEDS_REPAIR','NOT_ELIGIBLE')
```

## Test
Setelah migration berhasil:
1. Restart aplikasi: `./gradlew run`
2. Buka halaman Admin Products
3. Klik "Tambah Produk"
4. Pilih Status Kelayakan: "Perlu QC"
5. Isi form dan klik "Simpan"
6. Produk harus tersimpan dan muncul di Pipeline QC

## Files Updated
- ✅ `database/schema.sql` - Updated untuk future reference
- ✅ `database/add_needs_qc_status.sql` - Migration file (NEW)
