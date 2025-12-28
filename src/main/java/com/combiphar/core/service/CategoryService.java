package com.combiphar.core.service;

import java.util.List;

import com.combiphar.core.model.Category;
import com.combiphar.core.repository.CategoryRepository;

/**
 * Service for managing categories.
 */
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Get categories by status
     */
    public List<Category> getCategoriesByStatus(String status) {
        return categoryRepository.findByStatus(status);
    }

    /**
     * Get category by ID
     */
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));
    }

    /**
     * Create new category
     */
    public Category createCategory(String name, String description, String status) {
        // Validate duplicate name
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Kategori dengan nama tersebut sudah ada");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setStatus(status != null ? status : "AKTIF");

        return categoryRepository.save(category);
    }

    /**
     * Update existing category
     */
    public Category updateCategory(String id, String name, String description, String status) {
        // Check if category exists
        Category existingCategory = getCategoryById(id);

        // Validate duplicate name (excluding current category)
        categoryRepository.findByName(name).ifPresent(cat -> {
            if (!cat.getId().equals(id)) {
                throw new RuntimeException("Kategori dengan nama tersebut sudah ada");
            }
        });

        existingCategory.setName(name);
        existingCategory.setDescription(description);
        existingCategory.setStatus(status != null ? status : "AKTIF");

        return categoryRepository.update(id, existingCategory);
    }

    /**
     * Delete category
     */
    public void deleteCategory(String id) {
        // Check if category exists
        getCategoryById(id);

        boolean deleted = categoryRepository.deleteById(id);
        if (!deleted) {
            throw new RuntimeException("Gagal menghapus kategori");
        }
    }
}
