Berikut adalah pembagian tugas yang disusun secara kronologis berdasarkan alur kerja (flow), lengkap dengan nama branch, pesan commit, dan detail tugas spesifik untuk setiap personil:

### **Fase 1: Sistem Dasar & Keamanan (Kais Abiyyi)**

#### **Branch: feature/auth-and-security**

- **feat(core): setup database schema for users and products**
  - Membuat struktur tabel database berdasarkan _Physical Design_ (Users, Roles, Items).
  - Mengatur konfigurasi koneksi database di aplikasi Javalin.
- **feat(auth): implement customer registration and login routes**
  - Mengimplementasikan logika registrasi dan login untuk pelanggan di rute /profile.
  - Menangani enkripsi kata sandi dan manajemen sesi (_session management_).
- **feat(auth): implement admin login and session handling**
  - Membuat logika autentikasi khusus untuk admin di rute /admin/login.
- **feat(security): add RBAC middleware to protect /admin routes**
  - Implementasi _Role-Based Access Control_ (RBAC) untuk memastikan hanya admin yang bisa mengakses rute /admin.

### ---

**Fase 2: Manajemen Aset & Kontrol Kualitas (Nazar Muhammad Fikri)**

#### **Branch: feature/inventory-and-qc**

- **feat(inventory): implement category management logic**
  - Membuat fungsi CRUD untuk kategori barang pada rute /admin/category.
- **feat(inventory): create product CRUD and stock management**
  - Implementasi manajemen produk (tambah, edit, hapus) di rute /admin/products.
  - Menangani unggah foto barang dan pengisian SKU (seperti SKU: FUR-001).
- **feat(qc): implement eligibility status logic**
  - Membuat logika perubahan status barang berdasarkan pemeriksaan kelayakan (LAYAK JUAL, PERLU QC, LABEL ULANG).
- **feat(qc): add daily QC pipeline scheduling logic**
  - Implementasi tampilan dan data untuk "Pipeline QC Hari Ini" di _sidebar_ admin produk.

### ---

**Fase 3: Katalog & Penemuan Barang (Nazar Muhammad Fikri)**

#### **Branch: feature/customer-catalog**

- **feat(catalog): implement search and category filtering**
  - Menghubungkan kolom pencarian dan filter kategori pada halaman /catalog ke database.
- **feat(catalog): render dynamic product list with stock status**
  - Menampilkan daftar barang secara dinamis yang hanya berstatus "ELIGIBLE" (Layak Jual).
- **feat(pdp): implement product detail view with physical condition info**
  - Mengambil dan menampilkan data detail produk berdasarkan ID di rute /product/{id}.

### ---

**Fase 4: Alur Pemesanan & Transaksi (Jarwo Eddy Wicaksono)**

#### **Branch: feature/transaction-flow**

- **feat(cart): implement add to cart and quantity management**
  - Membuat logika penyimpanan item sementara di keranjang belanja pada rute /cart.
- **feat(checkout): create shipping address and courier selection form**
  - Menangani input formulir pengiriman (Nama, Alamat, Kota, Kode Pos) di halaman /checkout.
- **feat(order): implement order summary logic and subtotal calculation**
  - Menghitung otomatis subtotal, biaya logistik, dan total pembayaran di ringkasan pesanan.

### ---

**Fase 5: Pembayaran & Pemenuhan Pesanan (Jarwo Eddy Wicaksono)**

#### **Branch: feature/payment-and-shipping**

- **feat(payment): integrate QRIS payment gateway and automated callback**
  - Integrasi API pihak ketiga untuk generate kode QRIS pada rute /payment.
  - Membuat _webhook_ untuk pembaruan status pembayaran otomatis menjadi "PAID".
- **feat(admin-payment): implement manual cash verification**
  - Membuat fitur konfirmasi pembayaran tunai oleh admin di rute /admin/payments.
- **feat(shipping): implement receipt input and shipment tracking**
  - Implementasi input nomor resi dan pembaruan status kurir di rute /admin/shipping.
  - Menghubungkan data status pengiriman ke halaman pelacakan pelanggan /order/{id}.

### ---

**Fase 6: Dashboard & Analisis Akhir (Kais Abiyyi)**

#### **Branch: feature/admin-insights**

- **feat(admin-user): create CRUD API for user management**
  - Menyelesaikan fitur pengelolaan data pengguna di rute /admin/users.
- **feat(admin-dashboard): implement real-time stats**
  - Menampilkan ringkasan statistik (Total Produk, Stok Rendah, Produk Terjual) di halaman /admin/dashboard.
- **feat(admin-report): generate performance reports**
  - Implementasi logika penarikan laporan penjualan periodik pada rute /admin/reports.
