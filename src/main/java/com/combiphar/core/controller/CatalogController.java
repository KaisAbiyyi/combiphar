package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.combiphar.core.model.Category;
import com.combiphar.core.model.Item;
import com.combiphar.core.service.CategoryService;
import com.combiphar.core.service.ItemService;

import io.javalin.http.Context;

/**
 * Controller for customer-facing catalog pages.
 * Handles search, category filtering, and product listing.
 */
public class CatalogController {

    private final ItemService itemService;
    private final CategoryService categoryService;

    public CatalogController() {
        this.itemService = new ItemService();
        this.categoryService = new CategoryService();
    }

    /**
     * GET /catalog - Shows customer catalog page with search and filter
     */
    public void showCatalogPage(Context ctx) {
        String searchQuery = ctx.queryParam("search");
        String categoryId = ctx.queryParam("category");

        try {
            // Get all categories for filter dropdown
            List<Category> categories = categoryService.getAllCategories()
                    .stream()
                    .filter(c -> "AKTIF".equals(c.getStatus()))
                    .collect(Collectors.toList());

            // Search published items with filters
            List<Item> items = itemService.searchPublishedItems(searchQuery, categoryId);

            // Create category map for easy lookup
            Map<String, String> categoryMap = new HashMap<>();
            for (Category category : categories) {
                categoryMap.put(category.getId(), category.getName());
            }

            // Format items with category names for display
            List<Map<String, Object>> formattedItems = items.stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("name", item.getName());
                        itemMap.put("categoryId", item.getCategoryId());
                        itemMap.put("categoryName", categoryMap.getOrDefault(item.getCategoryId(), "Lainnya"));
                        itemMap.put("price", item.getPrice());
                        itemMap.put("stock", item.getStock());
                        itemMap.put("condition", item.getCondition());
                        itemMap.put("conditionLabel", getConditionLabel(item.getCondition()));
                        itemMap.put("description", item.getDescription());
                        itemMap.put("imageUrl", item.getImageUrl());
                        itemMap.put("eligibilityStatus", item.getEligibilityStatus());
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            // Build model
            Map<String, Object> model = new HashMap<>();
            model.put("title", "Katalog Produk - Combiphar Used Goods");
            model.put("activePage", "catalog");
            model.put("currentUser", ctx.sessionAttribute("currentUser"));
            model.put("items", formattedItems);
            model.put("categories", categories);
            model.put("totalItems", formattedItems.size());
            model.put("searchQuery", searchQuery != null ? searchQuery : "");
            model.put("currentCategory", categoryId != null ? categoryId : "");
            model.put("currentCategoryName",
                    categoryId != null ? categoryMap.getOrDefault(categoryId, "Semua Kategori") : "Semua Kategori");

            ctx.render("customer/catalog", model);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorModel = new HashMap<>();
            errorModel.put("title", "Error - Combiphar Used Goods");
            errorModel.put("activePage", "catalog");
            errorModel.put("currentUser", ctx.sessionAttribute("currentUser"));
            errorModel.put("items", List.of());
            errorModel.put("categories", List.of());
            errorModel.put("totalItems", 0);
            errorModel.put("searchQuery", "");
            errorModel.put("currentCategory", "");
            errorModel.put("currentCategoryName", "Semua Kategori");
            errorModel.put("error", "Terjadi kesalahan saat memuat katalog");
            ctx.render("customer/catalog", errorModel);
        }
    }

    /**
     * GET /product/{id} - Shows product detail page
     */
    public void showProductDetail(Context ctx) {
        String productId = ctx.pathParam("id");

        try {
            Item item = itemService.getItemById(productId);

            // Check if item is published and eligible
            if (!Boolean.TRUE.equals(item.getIsPublished()) || !"ELIGIBLE".equals(item.getEligibilityStatus())) {
                ctx.redirect("/catalog");
                return;
            }

            // Get category name
            String categoryName = "Lainnya";
            try {
                Category category = categoryService.getCategoryById(item.getCategoryId());
                categoryName = category.getName();
            } catch (Exception ignored) {
            }

            // Build product map
            Map<String, Object> product = new HashMap<>();
            product.put("id", item.getId());
            product.put("name", item.getName());
            product.put("categoryId", item.getCategoryId());
            product.put("categoryName", categoryName);
            product.put("price", item.getPrice());
            product.put("stock", item.getStock());
            product.put("condition", item.getCondition());
            product.put("conditionLabel", getConditionLabel(item.getCondition()));
            product.put("conditionGrade", getConditionGrade(item.getCondition()));
            product.put("description", item.getDescription());
            product.put("imageUrl", item.getImageUrl());

            // Build model
            Map<String, Object> model = new HashMap<>();
            model.put("title", item.getName() + " - Combiphar Used Goods");
            model.put("activePage", "product");
            model.put("currentUser", ctx.sessionAttribute("currentUser"));
            model.put("product", product);

            ctx.render("customer/product-detail", model);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.redirect("/catalog");
        }
    }

    /**
     * GET /api/catalog/search - API endpoint for searching products (AJAX)
     */
    public void searchProducts(Context ctx) {
        String searchQuery = ctx.queryParam("q");
        String categoryId = ctx.queryParam("category");

        try {
            List<Category> categories = categoryService.getAllCategories();
            Map<String, String> categoryMap = new HashMap<>();
            for (Category category : categories) {
                categoryMap.put(category.getId(), category.getName());
            }

            List<Item> items = itemService.searchPublishedItems(searchQuery, categoryId);

            List<Map<String, Object>> formattedItems = items.stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("name", item.getName());
                        itemMap.put("categoryName", categoryMap.getOrDefault(item.getCategoryId(), "Lainnya"));
                        itemMap.put("price", item.getPrice());
                        itemMap.put("stock", item.getStock());
                        itemMap.put("condition", item.getCondition());
                        itemMap.put("conditionLabel", getConditionLabel(item.getCondition()));
                        itemMap.put("imageUrl", item.getImageUrl());
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            ctx.json(Map.of(
                    "success", true,
                    "data", formattedItems,
                    "total", formattedItems.size()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Helper method to get human-readable condition label
     */
    private String getConditionLabel(String condition) {
        if (condition == null)
            return "Tidak Diketahui";

        switch (condition) {
            case "NEW":
                return "Baru";
            case "USED_GOOD":
                return "Bekas - Kondisi Baik";
            case "USED_FAIR":
                return "Bekas - Kondisi Cukup";
            case "DAMAGED":
                return "Rusak Ringan";
            default:
                return condition;
        }
    }

    /**
     * Helper method to get condition grade percentage
     */
    private String getConditionGrade(String condition) {
        if (condition == null)
            return "N/A";

        switch (condition) {
            case "NEW":
                return "Grade A - 100%";
            case "USED_GOOD":
                return "Grade A - 90%";
            case "USED_FAIR":
                return "Grade B - 75%";
            case "DAMAGED":
                return "Grade C - 60%";
            default:
                return "N/A";
        }
    }
}
