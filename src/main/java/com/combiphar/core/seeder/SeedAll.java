package com.combiphar.core.seeder;

/**
 * Master seeder that runs all individual seeders in correct order.
 * Run this to seed all data at once without connection pool issues.
 */
public class SeedAll {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   MASTER SEEDER - Run All Seeders");
        System.out.println("========================================\n");

        int exitCode = 0;

        try {
            // 1. Seed Users
            System.out.println("\n--- Step 1/5: Seeding Users ---");
            UserSeeder.main(args);

        } catch (Exception e) {
            System.err.println("✗ UserSeeder failed (continuing...): " + e.getMessage());
        }

        try {
            // 2. Seed Categories
            System.out.println("\n--- Step 2/5: Seeding Categories ---");
            CategorySeeder.main(args);

        } catch (Exception e) {
            System.err.println("✗ CategorySeeder failed (continuing...): " + e.getMessage());
        }

        try {
            // 3. Seed Items
            System.out.println("\n--- Step 3/5: Seeding Items ---");
            ProductSeeder.main(args);

        } catch (Exception e) {
            System.err.println("✗ ItemSeeder failed (continuing...): " + e.getMessage());
        }

        try {
            // 4. Seed Addresses
            System.out.println("\n--- Step 4/5: Seeding Addresses ---");
            AddressSeeder.main(args);

        } catch (Exception e) {
            System.err.println("✗ AddressSeeder failed (continuing...): " + e.getMessage());
        }

        try {
            // 5. Seed Orders
            System.out.println("\n--- Step 5/5: Seeding Orders ---");
            OrderSeeder.main(args);

        } catch (Exception e) {
            System.err.println("✗ OrderSeeder failed: " + e.getMessage());
            e.printStackTrace();
            exitCode = 1;
        }

        System.out.println("\n========================================");
        if (exitCode == 0) {
            System.out.println("✓ ALL SEEDERS COMPLETED SUCCESSFULLY!");
        } else {
            System.out.println("✗ SOME SEEDERS FAILED - Check logs above");
        }
        System.out.println("========================================\n");

        System.exit(exitCode);
    }
}
