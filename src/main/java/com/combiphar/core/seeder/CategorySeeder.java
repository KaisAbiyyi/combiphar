package com.combiphar.core.seeder;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Category;
import com.combiphar.core.repository.CategoryRepository;

/**
 * Seeder for creating sample categories.
 */
public class CategorySeeder {

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   Category Seeder");
        System.out.println("=====================================\n");

        try {
            // Initialize database connection
            DatabaseConfig.getConnection().close();
            System.out.println("✓ Database connection successful\n");

            CategoryRepository categoryRepository = new CategoryRepository();

            // Seed categories
            seedCategory(categoryRepository, "Medical Equipment", "Peralatan medis dan kesehatan");
            seedCategory(categoryRepository, "Laboratory", "Instrumen dan peralatan laboratorium");
            seedCategory(categoryRepository, "Pharmacy", "Peralatan farmasi dan apotek");
            seedCategory(categoryRepository, "Office Equipment", "Peralatan dan furniture kantor");
            seedCategory(categoryRepository, "Furnitur Eksekutif", "Meja, kursi, dan furniture kantor eksekutif");
            seedCategory(categoryRepository, "Elektronik Refurbished", "Perangkat elektronik yang telah diperbaharui");

            System.out.println("\n✓ All categories seeded successfully!\n");
            System.exit(0);

        } catch (Exception e) {
            System.err.println("✗ Error during seeding: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void seedCategory(CategoryRepository repository, String name, String description) {
        // Check if category already exists
        if (repository.existsByName(name)) {
            System.out.println("⚠ Category already exists: " + name);
            return;
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        repository.save(category);
        System.out.println("✓ Category created: " + name);
    }
}
