# UI Update - Admin Products Page

## Ringkasan Perubahan

Halaman admin products (`/admin/products`) telah diperbarui dengan UI baru yang lebih modern dan fungsional berdasarkan screenshot yang diberikan.

## Fitur Yang Dihilangkan

1. ❌ **Button "Tambah Produk"** - Dihapus dari UI utama
2. ❌ **Search box** - Tidak ada di UI baru
3. ❌ **Button "Ekspor" dan "Import" di area tabel** - Diganti dengan fitur baru di header
4. ❌ **Pagination** - Dihapus dari tampilan tabel
5. ❌ **Checkbox selection** di kolom pertama tabel
6. ❌ **Stats cards lama** (Total Produk, Stok Rendah, Nilai Total)

## Fitur Baru Yang Ditambahkan

### 1. **Header Section**
- Page title: "Manajemen Inventory / Katalog Produk"
- Button "Unduh Routing" - untuk download file routing warehouse
- Button "Impor CSV" - untuk import bulk produk dari CSV
- Input file hidden yang trigger saat klik "Impor CSV"

### 2. **Stats Cards Baru**
- **TOTAL SKU**: Menampilkan jumlah total SKU produk
- **PERLU QC**: Menampilkan jumlah produk yang perlu QC dengan subtitle jumlah yang butuh review
  - Style warning (border kuning, background kuning muda)
  - Contoh: "18" dengan subtitle "6 butuh pemeriksaan ulang QC"
- **NILAI INVENTARIS**: Total nilai inventaris dalam jutaan rupiah
  - Format: Rp X,X M
- **ROTASI SKU**: Rata-rata hari rotasi SKU
  - Format: XX hari

### 3. **Filter Dropdown**
Dua dropdown filter terpisah:
- **Filter Kategori**: Dropdown untuk filter berdasarkan kategori produk
  - Menampilkan semua kategori yang tersedia
  - Opsi "Semua Kategori" untuk reset filter
- **Status Kelayakan**: Dropdown untuk filter berdasarkan status
  - Layak Jual
  - Perlu QC
  - Perlu Perbaikan
  - Tidak Layak
  - Opsi "Semua Status" untuk reset

### 4. **Table Columns Updated**
Kolom yang ditampilkan:
1. **SKU** (baru) - Format: SKU-XXXX dengan badge styling
2. **NAMA BARANG** - Nama produk
3. **KATEGORI** - Nama kategori
4. **HARGA** - Harga satuan (format rupiah)
5. **STOK** - Jumlah unit/set
6. **STATUS** - Status kelayakan dengan badge warna
7. **AKSI** - Conditional action buttons

### 5. **Conditional Action Buttons**
Actions berbeda berdasarkan status produk:
- **Layak Jual**: Edit + Lihat
- **Perlu QC**: Edit + Batalkan QC
- **Perlu Perbaikan**: Edit + Bundel
- **Tidak Layak**: Edit + Lihat

### 6. **Quick Update Stok Sidebar**
Form untuk update stok cepat tanpa perlu edit full produk:
- Input **SKU**: Input text untuk masukkan SKU produk (contoh: SKU-4420)
- Dropdown **Aksi**: 
  - Tambah Stok (add)
  - Kurangi Stok (remove)
  - Set Stok (set)
- Input **Jumlah**: Input number untuk quantity
- Button **Simpan Perubahan**: Submit form

## API Endpoints Baru

### 1. Quick Update Stock
```
POST /api/admin/items/{id}/update-stock
Content-Type: application/json

Request Body:
{
  "action": "add" | "remove" | "set",
  "quantity": number
}

Response:
{
  "success": true,
  "message": "Stok berhasil diupdate dari X menjadi Y unit"
}
```

### 2. Cancel QC
```
POST /api/admin/items/{id}/cancel-qc

Response:
{
  "success": true,
  "message": "Status QC berhasil dibatalkan, produk dikembalikan ke status 'Layak Jual'"
}
```

### 3. CSV Import (Placeholder)
```
POST /api/admin/items/import-csv
Content-Type: multipart/form-data

Request Body:
- file: CSV file

Response:
{
  "success": true,
  "imported": number,
  "failed": number
}
```

## JavaScript Functions Baru

### Filter Functions
- `toggleCategoryFilter()` - Toggle dropdown filter kategori
- `toggleStatusFilter()` - Toggle dropdown filter status
- Click outside to close dropdowns

### Action Functions
- `editProduct(productId)` - Edit produk (placeholder)
- `viewProduct(productId)` - Lihat detail produk (placeholder)
- `cancelQC(productId)` - Batalkan status QC produk
- `bundleProduct(productId)` - Bundle produk (placeholder)

### Feature Functions
- `downloadRouting()` - Download file routing (placeholder)
- `handleCSVImport(event)` - Handle CSV file import
- `handleQuickUpdateStock(event)` - Handle form submit quick update

## CSS Styling

File: `/css/pages/admin/products.css`

### Key Classes
- `.page-header` - Header dengan title dan action buttons
- `.stats-grid` - Grid layout untuk 4 stat cards
- `.stat-card` - Individual stat card
- `.stat-card--warning` - Warning variant (untuk Perlu QC)
- `.product-sku` - Badge styling untuk SKU
- `.filter-dropdown` - Dropdown filter dengan toggle button
- `.quick-update-card` - Card untuk quick update form
- `.btn--block` - Full width button

## Backend Changes

### ItemController.java

#### New Methods:
1. `quickUpdateStock(Context ctx)` - Handle quick stock update
2. `cancelQC(Context ctx)` - Handle cancel QC request

#### Updated showProductPage():
New model attributes:
- `totalSKU` - Total jumlah SKU
- `needsQCCount` - Jumlah produk perlu QC
- `qcReviewCount` - Jumlah produk perlu review QC
- `totalValueInMillions` - Nilai inventaris dalam juta
- `rotationDays` - Rata-rata hari rotasi

### Main.java

New routes added:
```java
app.post("/api/admin/items/{id}/update-stock", itemController::quickUpdateStock);
app.post("/api/admin/items/{id}/cancel-qc", itemController::cancelQC);
```

## Testing Checklist

### Frontend
- [ ] Page loads successfully at `/admin/products`
- [ ] Stats cards show correct data
- [ ] Category filter dropdown works
- [ ] Status filter dropdown works
- [ ] Filters can be reset to "All"
- [ ] SKU column displays with proper formatting
- [ ] Conditional action buttons show based on status
- [ ] Quick Update Stok form visible in sidebar
- [ ] CSV import button triggers file input
- [ ] Unduh Routing button shows alert (placeholder)

### Backend
- [ ] Stats calculations correct:
  - Total SKU count
  - Needs QC count
  - QC review count
  - Total value in millions
  - Rotation days
- [ ] Quick update stock API works:
  - Add stock action
  - Remove stock action
  - Set stock action
  - Error handling for invalid SKU
- [ ] Cancel QC API works:
  - Status changes from NEEDS_QC to ELIGIBLE
  - Error when trying to cancel non-QC items
  - Success message returned

### Database
- [ ] NEEDS_QC status exists in ENUM (run migration if not)
- [ ] Stock updates persist correctly
- [ ] Status updates persist correctly

## Known Placeholders (TODO)

Features marked as placeholder yang belum fully implemented:

1. **Tambah Produk** - Modal form dihapus, perlu implement halaman terpisah
2. **Edit Product** - Button ada tapi redirect belum implement
3. **View Product** - Button ada tapi detail page belum ada
4. **Bundle Product** - Button ada tapi form bundling belum ada
5. **CSV Import** - API endpoint belum implement, hanya alert
6. **Download Routing** - Button ada tapi download file belum implement
7. **QC Review Count** - Placeholder calculation (max 6), perlu implement dengan tracking date

## Migration Required

Sebelum menggunakan fitur ini, pastikan menjalankan migration:

```sql
-- File: database/add_needs_qc_status.sql
ALTER TABLE items 
MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') 
NOT NULL DEFAULT 'ELIGIBLE';
```

Lihat `RUN_MIGRATION.md` untuk instruksi detail.

## File Structure

```
src/main/resources/
├── static/
│   └── css/
│       └── pages/
│           └── admin/
│               └── products.css (NEW)
├── templates/
│   └── admin/
│       └── product.pebble (UPDATED)
src/main/java/
├── Main.java (UPDATED - added routes)
└── com/combiphar/core/controller/
    └── ItemController.java (UPDATED - added methods)
```

## Screenshots Reference

UI ini dibuat berdasarkan screenshot yang menunjukkan:
- Header "Katalog Produk" dengan 2 action buttons
- 4 stat cards horizontal
- 2 dropdown filters (Category dan Status)
- Table dengan kolom SKU
- Sidebar dengan Pipeline QC dan Quick Update Stok
- Conditional action buttons
