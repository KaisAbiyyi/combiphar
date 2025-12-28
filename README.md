# Combiphar Used Goods Core

Backend core (prototype) for PT Combiphar's **Second-Hands Goods Sales System**.

This repository currently contains a web app skeleton built with **Java 11 + Gradle + Javalin**, using **Pebble** templates, plus an initial database schema draft.

## Features (based on PRD/SRS/SDD)

Target system capabilities:
- Used-goods inventory and category management
- Transactions (cart/checkout) and order creation
- Payments: QRIS (automatic) and cash (manual admin validation)
- Shipment tracking (tracking number and status)
- User management with roles (RBAC)
- Sales and stock reporting

Note: the current codebase is still at an early stage (example route + template rendering).

## Prerequisites

- JDK 11
- MySQL Server (XAMPP, MySQL Workbench, atau standalone MySQL)
- Internet access to download Gradle dependencies (first run)

## Database Setup

1. **Create Database**
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   
   Or via MySQL Workbench/phpMyAdmin:
   - Create database: `combiphar_db`
   - Execute `database/schema.sql`

2. **Run Migrations (IMPORTANT!)**
   ```bash
   mysql -u root -p combiphar_db < database/add_needs_qc_status.sql
   ```
   
   Or manually run the ALTER TABLE query:
   ```sql
   USE combiphar_db;
   ALTER TABLE items 
   MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE') DEFAULT 'ELIGIBLE';
   ```
   
   **⚠️ Without this migration, you cannot save products with "Perlu QC" status!**
   
   See [RUN_MIGRATION.md](RUN_MIGRATION.md) for detailed instructions.

3. **Verify Database Connection**
   
   The app uses these default settings:
   - URL: `jdbc:mysql://localhost:3306/combiphar_db`
   - User: `root`
   - Password: (empty)
   
   You can override via environment variables:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/combiphar_db"
   export DB_USER="root"
   export DB_PASS="your_password"
   ```

## Run

Windows (PowerShell/CMD):
- `gradlew.bat run`

Linux/macOS (bash):
- `./gradlew run`

The app runs at `http://localhost:7070/`.

## Build

- `./gradlew clean build`

## Project structure

- `src/main/java/Main.java` — Javalin entry point
- `src/main/resources/templates/` — Pebble templates (e.g., `home.pebble`)
- `database/schema.sql` — draft tables (users, categories, items, orders, order_items, payments, shipments)
- `docs/prd.md` — PRD
- `docs/SRS_Second-Hands_Goods_Sales_System.docx` — SRS
- `docs/SDD_Used-Goods.docx` — SDD

## Technical notes

- Template engine: Pebble via `io.javalin:javalin-rendering`.
- Default port: `7070`.

## Recent Updates

### UI Redesign - Admin Products Page (Latest)
Halaman admin products telah diperbarui dengan UI baru yang lebih modern:
- **New Stats Cards**: Total SKU, Perlu QC (with warning), Nilai Inventaris, Rotasi SKU
- **Enhanced Filters**: Separate dropdowns for Category and Status
- **SKU Column**: Display product SKU in table (format: SKU-XXXX)
- **Conditional Actions**: Different action buttons based on product status
- **Quick Update Stock**: Sidebar form for fast stock updates
- **CSV Import**: Bulk product import feature (header button)

Lihat [docs/UI_UPDATE_PRODUCTS.md](docs/UI_UPDATE_PRODUCTS.md) untuk detail lengkap.

### Database Migration
Added `NEEDS_QC` status to eligibility_status ENUM:
```sql
ALTER TABLE items 
MODIFY COLUMN eligibility_status ENUM('ELIGIBLE', 'NEEDS_QC', 'NEEDS_REPAIR', 'NOT_ELIGIBLE');
```

See [RUN_MIGRATION.md](RUN_MIGRATION.md) for instructions.

