# Database Setup & Migration

## Overview

This project uses a single comprehensive database schema (`schema_v2.sql`) that includes all tables and features. The schema supports:

- User management (CUSTOMER, ADMIN roles)
- Category management
- Item/Product management with QC pipeline
- Shopping cart (persistent)
- Address management
- Order management
- Payment system (Transfer method with BCA, MANDIRI, BRI)
- Shipment tracking

## Quick Start

### Fresh Installation

```bash
# 1. Set environment variables
export DB_URL="jdbc:mysql://localhost:3306/combiphar_db?useSSL=false&serverTimezone=Asia/Jakarta"
export DB_USER="root"
export DB_PASS="your_password"

# 2. Run schema v2
mysql -u root -p < database/schema_v2.sql

# 3. Run seeders in order
./gradlew seedUser     # Creates users (admin, customers)
./gradlew seedCategory # Creates categories
./gradlew seedItem     # Creates items/products
./gradlew seedAddress  # Creates addresses
./gradlew seedOrder    # Creates orders with payments & shipments
```

### Seeder Execution Order

Run seeders in this specific order to respect foreign key dependencies:

```
1. UserSeeder      → Creates users
2. CategorySeeder  → Creates categories
3. ItemSeeder      → Creates items (requires categories)
4. AddressSeeder   → Creates addresses (requires users)
5. OrderSeeder     → Creates orders (requires users, addresses, items)
```

## Available Seeders

| Seeder | Description | Defensive |
|--------|-------------|-----------|
| UserSeeder | Creates admin, owner, and test customers | ✅ Can run multiple times |
| CategorySeeder | Creates product categories | ✅ Can run multiple times |
| ItemSeeder (ProductSeeder) | Creates test items/products | ✅ Can run multiple times |
| AddressSeeder | Creates test addresses | ✅ Can run multiple times |
| OrderSeeder | Creates test orders with payments & shipments | ✅ Can run multiple times |

## Default Login Credentials

After running `UserSeeder`:

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@combiphar.com | Admin123456 |
| Customer | customer1@test.com | Customer123 |

## Database Tables

### Core Tables

- **users** - User accounts with roles (CUSTOMER, ADMIN)
- **categories** - Product categories with status (AKTIF, REVIEW, DRAFT)
- **items** - Products with QC pipeline (ELIGIBLE, NEEDS_QC, NEEDS_REPAIR, NOT_ELIGIBLE)

### Cart Tables

- **carts** - Shopping cart per user
- **cart_items** - Items in cart with snapshot pricing

### Order Tables

- **addresses** - User delivery addresses
- **orders** - Order headers with payment & shipping status
- **order_items** - Order line items with notes

### Payment & Shipment

- **payments** - Payment records (TRANSFER method: BCA, MANDIRI, BRI)
- **shipments** - Shipment tracking with courier info

## Schema Features

### QC Pipeline for Items

Items can have these eligibility statuses:
- `ELIGIBLE` - Ready for sale
- `NEEDS_QC` - Requires quality check
- `NEEDS_REPAIR` - Needs repair/relabeling
- `NOT_ELIGIBLE` - Not suitable for sale

### Payment Methods

Only TRANSFER method is supported with these banks:
- `BCA` - Bank Central Asia
- `MANDIRI` - Mandiri Bank
- `BRI` - Bank Rakyat Indonesia

### Shipment Status

Orders track shipment through these stages:
- `PENDING` - Waiting for confirmation
- `PROCESSING` - Being prepared
- `SHIPPED` - In transit
- `DELIVERED` - Delivered to address
- `RECEIVED` - Order completed

## Old Migration Files (Deprecated)

The following files are deprecated and replaced by `schema_v2.sql`:

- ~~`schema.sql`~~ - Use `schema_v2.sql` instead
- ~~`migrations.sql`~~ - Merged into `schema_v2.sql`
- ~~`migration_add_cart.sql`~~ - Merged into `schema_v2.sql`
- ~~`migration_address_and_notes.sql`~~ - Merged into `schema_v2.sql`
- ~~`migration_update_payment_to_transfer.sql`~~ - Merged into `schema_v2.sql`
- ~~`migration_update_shipment_status.sql`~~ - Merged into `schema_v2.sql`

## Troubleshooting

### "Environment variable DB_URL not set"

Make sure to set all required environment variables before running the application:

```bash
export DB_URL="jdbc:mysql://localhost:3306/combiphar_db?useSSL=false&serverTimezone=Asia/Jakarta"
export DB_USER="root"
export DB_PASS="your_password"
```

### Seeder says "already exists"

This is normal behavior. All seeders use defensive programming - they check for duplicates before inserting. You can safely run seeders multiple times.

### No users found when running AddressSeeder

Make sure to run `UserSeeder` first before running `AddressSeeder`.

### No items found when running OrderSeeder

Make sure to run `ItemSeeder` first before running `OrderSeeder`.

## Verification

After setup, verify with:

```sql
-- Check all tables
SHOW TABLES;

-- Check users
SELECT email, role FROM users;

-- Check items per status
SELECT eligibility_status, COUNT(*) FROM items GROUP BY eligibility_status;

-- Check orders
SELECT order_number, status_payment, status_order FROM orders;
```

## Important Notes

- All seeders are **idempotent** - safe to run multiple times
- Foreign key constraints are properly set up
- Indexes are created for performance
- No `products` table exists - use `items` table instead
- All payment methods use TRANSFER with bank selection
