package com.combiphar.core.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.combiphar.core.model.Category;
import com.combiphar.core.model.User;
import com.combiphar.core.service.CategoryService;

import io.javalin.http.Context;

/**
 * Controller for category management routes.
 */
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryService();
    }

    /**
     * GET /admin/categories - Shows category list page
     */
    public void showCategoryPage(Context ctx) {
        User currentUser = ctx.sessionAttribute("currentUser");

        if (currentUser == null) {
            ctx.redirect("/admin/login");
            return;
        }

        try {
            String statusFilter = ctx.queryParam("status");

            // Always fetch all categories for stats
            List<Category> allCategories = categoryService.getAllCategories();

            // Fetch filtered categories for table
            List<Category> filteredCategories;
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("all")) {
                filteredCategories = categoryService.getCategoriesByStatus(statusFilter);
            } else {
                filteredCategories = allCategories;
            }

            // Count categories by status for stats
            long totalCategories = allCategories.size();
            long activeCategories = allCategories.stream()
                    .filter(c -> "AKTIF".equals(c.getStatus()))
                    .count();

            // Convert filtered categories to maps with formatted dates for template
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            List<Map<String, Object>> categoryMaps = new ArrayList<>();

            for (Category category : filteredCategories) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("id", category.getId());
                categoryMap.put("name", category.getName());
                categoryMap.put("description", category.getDescription());
                categoryMap.put("status", category.getStatus());

                // Format dates as strings
                if (category.getUpdatedAt() != null) {
                    categoryMap.put("updatedAt", category.getUpdatedAt().format(formatter));
                } else if (category.getCreatedAt() != null) {
                    categoryMap.put("updatedAt", category.getCreatedAt().format(formatter));
                } else {
                    categoryMap.put("updatedAt", "N/A");
                }

                categoryMaps.add(categoryMap);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("title", "Manajemen Kategori");
            model.put("currentUser", currentUser);
            model.put("activePage", "category");
            model.put("categories", categoryMaps);
            model.put("totalCategories", totalCategories);
            model.put("activeCategories", activeCategories);
            model.put("currentFilter", statusFilter != null ? statusFilter : "all");

            ctx.render("admin/category", model);
        } catch (Exception e) {
            ctx.status(500);
            ctx.result("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/admin/categories - Get all categories (API)
     */
    public void getAllCategories(Context ctx) {
        try {
            List<Category> categories = categoryService.getAllCategories();

            // Convert to simple maps for JSON response
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            List<Map<String, Object>> categoryMaps = new ArrayList<>();

            for (Category category : categories) {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("id", category.getId());
                categoryMap.put("name", category.getName());
                categoryMap.put("description", category.getDescription());
                categoryMap.put("status", category.getStatus());

                if (category.getUpdatedAt() != null) {
                    categoryMap.put("updatedAt", category.getUpdatedAt().format(formatter));
                } else if (category.getCreatedAt() != null) {
                    categoryMap.put("updatedAt", category.getCreatedAt().format(formatter));
                } else {
                    categoryMap.put("updatedAt", "N/A");
                }

                categoryMaps.add(categoryMap);
            }

            ctx.json(Map.of(
                    "success", true,
                    "data", categoryMaps));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/categories/:id - Get category by ID (API)
     */
    public void getCategoryById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Category category = categoryService.getCategoryById(id);

            // Convert to simple map for JSON response
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getId());
            categoryMap.put("name", category.getName());
            categoryMap.put("description", category.getDescription());
            categoryMap.put("status", category.getStatus());

            if (category.getUpdatedAt() != null) {
                categoryMap.put("updatedAt", category.getUpdatedAt().format(formatter));
            } else if (category.getCreatedAt() != null) {
                categoryMap.put("updatedAt", category.getCreatedAt().format(formatter));
            } else {
                categoryMap.put("updatedAt", "N/A");
            }

            ctx.json(Map.of(
                    "success", true,
                    "data", categoryMap));
        } catch (Exception e) {
            ctx.status(404).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/categories - Create new category (API)
     */
    public void createCategory(Context ctx) {
        try {
            String name = ctx.formParam("name");
            String description = ctx.formParam("description");
            String status = ctx.formParam("status");

            if (name == null || name.trim().isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Nama kategori tidak boleh kosong"));
                return;
            }

            Category category = categoryService.createCategory(name, description, status);

            // Convert to simple map for JSON response
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getId());
            categoryMap.put("name", category.getName());
            categoryMap.put("description", category.getDescription());
            categoryMap.put("status", category.getStatus());

            if (category.getUpdatedAt() != null) {
                categoryMap.put("updatedAt", category.getUpdatedAt().format(formatter));
            } else if (category.getCreatedAt() != null) {
                categoryMap.put("updatedAt", category.getCreatedAt().format(formatter));
            } else {
                categoryMap.put("updatedAt", "N/A");
            }

            ctx.status(201).json(Map.of(
                    "success", true,
                    "message", "Kategori berhasil dibuat",
                    "data", categoryMap));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/categories/:id - Update category (API)
     */
    public void updateCategory(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            String name = ctx.formParam("name");
            String description = ctx.formParam("description");
            String status = ctx.formParam("status");

            if (name == null || name.trim().isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Nama kategori tidak boleh kosong"));
                return;
            }

            Category category = categoryService.updateCategory(id, name, description, status);

            // Convert to simple map for JSON response
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getId());
            categoryMap.put("name", category.getName());
            categoryMap.put("description", category.getDescription());
            categoryMap.put("status", category.getStatus());

            if (category.getUpdatedAt() != null) {
                categoryMap.put("updatedAt", category.getUpdatedAt().format(formatter));
            } else if (category.getCreatedAt() != null) {
                categoryMap.put("updatedAt", category.getCreatedAt().format(formatter));
            } else {
                categoryMap.put("updatedAt", "N/A");
            }

            ctx.json(Map.of(
                    "success", true,
                    "message", "Kategori berhasil diperbarui",
                    "data", categoryMap));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/categories/:id - Delete category (API)
     */
    public void deleteCategory(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            categoryService.deleteCategory(id);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Kategori berhasil dihapus"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
