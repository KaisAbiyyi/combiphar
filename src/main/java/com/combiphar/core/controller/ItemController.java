package com.combiphar.core.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.combiphar.core.model.Category;
import com.combiphar.core.model.Item;
import com.combiphar.core.model.User;
import com.combiphar.core.service.CategoryService;
import com.combiphar.core.service.ItemService;
import com.combiphar.core.service.QualityCheckService;
import com.combiphar.core.util.CsvUtils;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

/**
 * Controller for item/product management routes.
 */
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;
    private final QualityCheckService qcService;

    public ItemController() {
        this.itemService = new ItemService();
        this.categoryService = new CategoryService();
        this.qcService = new QualityCheckService();
    }

    /**
     * GET /admin/products - Shows product management page
     */
    public void showProductPage(Context ctx) {
        User currentUser = ctx.sessionAttribute("currentUser");

        if (currentUser == null) {
            ctx.redirect("/admin/login");
            return;
        }

        try {
            // Get filter parameters
            String statusFilter = ctx.queryParam("status");
            String stockFilter = ctx.queryParam("stock");
            String categoryFilter = ctx.queryParam("categoryId");

            List<Item> items = itemService.getAllItems();
            List<Category> categories = categoryService.getAllCategories();

            // Apply filters
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("all")) {
                items = items.stream()
                        .filter(i -> statusFilter.equals(i.getEligibilityStatus()))
                        .collect(java.util.stream.Collectors.toList());
            }

            if (stockFilter != null && !stockFilter.isEmpty() && !stockFilter.equals("all")) {
                switch (stockFilter) {
                    case "available":
                        items = items.stream()
                                .filter(i -> i.getStock() != null && i.getStock() >= 5)
                                .collect(java.util.stream.Collectors.toList());
                        break;
                    case "low":
                        items = items.stream()
                                .filter(i -> i.getStock() != null && i.getStock() > 0 && i.getStock() < 5)
                                .collect(java.util.stream.Collectors.toList());
                        break;
                    case "out":
                        items = items.stream()
                                .filter(i -> i.getStock() != null && i.getStock() == 0)
                                .collect(java.util.stream.Collectors.toList());
                        break;
                }
            }

            if (categoryFilter != null && !categoryFilter.isEmpty() && !categoryFilter.equals("all")) {
                items = items.stream()
                        .filter(i -> categoryFilter.equals(i.getCategoryId()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Get all items for stats (before filtering)
            List<Item> allItems = itemService.getAllItems();

            // Calculate new stats for the UI
            long totalSKU = allItems.size();

            // Count items that need QC
            long needsQCCount = allItems.stream()
                    .filter(i -> "NEEDS_QC".equals(i.getEligibilityStatus()))
                    .count();

            // Count items that need QC review (items that have been in QC for > 7 days)
            // For now, we'll use a simple count - you can enhance this with actual date
            // tracking
            long qcReviewCount = Math.min(needsQCCount, 6); // Placeholder: max 6 items need review

            // Calculate total inventory value (in millions)
            BigDecimal totalValue = allItems.stream()
                    .map(i -> {
                        BigDecimal price = i.getPrice() != null ? i.getPrice() : BigDecimal.ZERO;
                        int stock = i.getStock() != null ? i.getStock() : 0;
                        return price.multiply(BigDecimal.valueOf(stock));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            DecimalFormat currencyFormat = new DecimalFormat("#,###");
            String totalValueFormatted = currencyFormat.format(totalValue);

            // Calculate average rotation days (placeholder calculation)
            // In a real app, you'd calculate this based on actual sales data
            int rotationDays = 45; // Average days for SKU rotation

            // Old stats (kept for compatibility)
            long totalProducts = allItems.size();
            long lowStockCount = allItems.stream().filter(i -> i.getStock() != null && i.getStock() < 5).count();

            // Create category map for easy lookup
            Map<String, String> categoryMap = new HashMap<>();
            for (Category category : categories) {
                categoryMap.put(category.getId(), category.getName());
            }

            // Format items with category names
            List<Map<String, Object>> formattedItems = new java.util.ArrayList<>();
            for (Item item : items) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("name", item.getName());
                itemMap.put("categoryId", item.getCategoryId());
                itemMap.put("categoryName", categoryMap.getOrDefault(item.getCategoryId(), "N/A"));
                itemMap.put("price", item.getPrice());
                itemMap.put("stock", item.getStock());
                itemMap.put("condition", item.getCondition());
                itemMap.put("description", item.getDescription());
                itemMap.put("imageUrl", item.getImageUrl());
                itemMap.put("eligibilityStatus", item.getEligibilityStatus());
                itemMap.put("isPublished", item.getIsPublished());
                formattedItems.add(itemMap);
            }

            // Get QC Pipeline for today
            List<Item> qcPipeline = qcService.getTodayQCPipeline();
            List<Map<String, Object>> qcPipelineFormatted = new java.util.ArrayList<>();

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH.mm");
            LocalDateTime now = LocalDateTime.now();

            for (int i = 0; i < Math.min(qcPipeline.size(), 3); i++) {
                Item item = qcPipeline.get(i);
                Map<String, Object> qcMap = new HashMap<>();

                // Generate time slots (8:30, 11:00, 15:30)
                LocalDateTime scheduleTime = now.withHour(8).withMinute(30).plusHours(i * 3);
                qcMap.put("time", scheduleTime.format(timeFormatter));
                qcMap.put("productName", item.getName());
                qcMap.put("categoryName", categoryMap.getOrDefault(item.getCategoryId(), "N/A"));

                // Generate warehouse and PIC info
                String[] warehouses = { "Gudang Bekasi · PIC: Vina", "Gudang Cikarang · PIC: Yudha",
                        "Gudang Sunter · PIC: Rio" };
                qcMap.put("location", warehouses[i % warehouses.length]);

                // Add notes based on status
                if (item.getStock() != null && item.getStock() < 5) {
                    qcMap.put("note", "Status: Stok rendah, perlu pengecekan tambahan");
                    qcMap.put("isAlert", true);
                } else {
                    qcMap.put("note", "Perlu cek kondisi fisik & kelengkapan");
                    qcMap.put("isAlert", false);
                }

                qcPipelineFormatted.add(qcMap);
            }

            Map<String, Object> model = new HashMap<>();
            model.put("title", "Manajemen Produk");
            model.put("currentUser", currentUser);
            model.put("activePage", "product");
            model.put("items", formattedItems);
            model.put("categories", categories);

            // New stats for updated UI
            model.put("totalSKU", totalSKU);
            model.put("needsQCCount", needsQCCount);
            model.put("qcReviewCount", qcReviewCount);
            model.put("totalValueFormatted", totalValueFormatted);
            model.put("rotationDays", rotationDays);

            // Old stats (kept for compatibility)
            model.put("totalProducts", totalProducts);
            model.put("lowStockCount", lowStockCount);
            model.put("totalValue", totalValue);

            model.put("currentFilter", statusFilter != null ? statusFilter : "all");
            model.put("currentCategoryFilter", categoryFilter != null ? categoryFilter : "all");
            model.put("currentStockFilter", stockFilter != null ? stockFilter : "all");
            model.put("qcPipeline", qcPipelineFormatted);
            model.put("qcCount", qcPipelineFormatted.size());

            ctx.render("admin/product", model);
        } catch (Exception e) {
            ctx.status(500);
            ctx.result("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/admin/items - Get all items (API)
     */
    public void getAllItems(Context ctx) {
        try {
            String categoryId = ctx.queryParam("categoryId");
            String status = ctx.queryParam("status");

            List<Item> items;

            if (categoryId != null) {
                items = itemService.getItemsByCategory(categoryId);
            } else if (status != null) {
                items = itemService.getItemsByEligibilityStatus(status);
            } else {
                items = itemService.getAllItems();
            }

            ctx.json(Map.of(
                    "success", true,
                    "data", items));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/items/:id - Get item by ID (API)
     */
    public void getItemById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Item item = itemService.getItemById(id);

            ctx.json(Map.of(
                    "success", true,
                    "data", item));
        } catch (Exception e) {
            ctx.status(404).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/items/published - Get published items for customer catalog (API)
     */
    public void getPublishedItems(Context ctx) {
        try {
            List<Item> items = itemService.getPublishedItems();

            ctx.json(Map.of(
                    "success", true,
                    "data", items));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/items - Create new item (API)
     */
    public void createItem(Context ctx) {
        try {
            String categoryId = ctx.formParam("categoryId");
            String name = ctx.formParam("name");
            String condition = ctx.formParam("condition");
            String description = ctx.formParam("description");
            String priceStr = ctx.formParam("price");
            String stockStr = ctx.formParam("stock");
            String eligibilityStatus = ctx.formParam("eligibilityStatus");
            String isPublishedStr = ctx.formParam("isPublished");

            // Validation
            if (categoryId == null || name == null || condition == null ||
                    priceStr == null || stockStr == null) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Semua field wajib diisi"));
                return;
            }

            BigDecimal price = new BigDecimal(priceStr);
            Integer stock = Integer.parseInt(stockStr);
            Boolean isPublished = isPublishedStr != null && isPublishedStr.equals("true");

            // Set default values if not provided
            if (eligibilityStatus == null || eligibilityStatus.isEmpty()) {
                eligibilityStatus = "ELIGIBLE";
            }

            // Handle image upload
            String imageUrl = null;
            UploadedFile uploadedFile = ctx.uploadedFile("image");
            if (uploadedFile != null && uploadedFile.size() > 0) {
                imageUrl = saveUploadedImage(uploadedFile);
            }

            Item item = itemService.createItem(categoryId, name, condition,
                    description, price, stock, eligibilityStatus, isPublished, imageUrl);

            ctx.status(201).json(Map.of(
                    "success", true,
                    "message", "Item berhasil dibuat",
                    "data", item));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Format harga atau stok tidak valid"));
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            // Check if it's an ENUM constraint violation for NEEDS_QC
            if (errorMessage != null && (errorMessage.contains("Data truncated") ||
                    errorMessage.contains("Incorrect") || errorMessage.contains("ENUM")) &&
                    errorMessage.contains("eligibility_status")) {
                errorMessage = "Status 'NEEDS_QC' belum didukung di database. " +
                        "Silakan jalankan migration: database/add_needs_qc_status.sql. " +
                        "Lihat file RUN_MIGRATION.md untuk instruksi lengkap.";
            }

            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", errorMessage));
        }
    }

    /**
     * PUT /api/admin/items/:id - Update item (API)
     */
    public void updateItem(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            String categoryId = ctx.formParam("categoryId");
            String name = ctx.formParam("name");
            String condition = ctx.formParam("condition");
            String description = ctx.formParam("description");
            String priceStr = ctx.formParam("price");
            String stockStr = ctx.formParam("stock");
            String eligibilityStatus = ctx.formParam("eligibilityStatus");
            String isPublishedStr = ctx.formParam("isPublished");

            // Validation
            if (categoryId == null || name == null || condition == null ||
                    priceStr == null || stockStr == null) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Semua field wajib diisi"));
                return;
            }

            BigDecimal price = new BigDecimal(priceStr);
            Integer stock = Integer.parseInt(stockStr);
            Boolean isPublished = isPublishedStr != null && isPublishedStr.equals("true");

            // Handle image upload
            String imageUrl = null;
            UploadedFile uploadedFile = ctx.uploadedFile("image");
            if (uploadedFile != null && uploadedFile.size() > 0) {
                imageUrl = saveUploadedImage(uploadedFile);
            }

            Item item = itemService.updateItem(id, categoryId, name, condition,
                    description, price, stock, eligibilityStatus, isPublished, imageUrl);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Item berhasil diperbarui",
                    "data", item));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Format harga atau stok tidak valid"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/items/:id/status - Update item eligibility status (API)
     */
    public void updateItemStatus(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            String status = ctx.formParam("status");

            if (status == null) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Status wajib diisi"));
                return;
            }

            boolean updated = itemService.updateEligibilityStatus(id, status);

            if (updated) {
                ctx.json(Map.of(
                        "success", true,
                        "message", "Status kelayakan berhasil diperbarui"));
            } else {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Gagal memperbarui status"));
            }
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/items/:id/stock - Update item stock (API)
     */
    public void updateItemStock(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            String quantityStr = ctx.formParam("quantity");

            if (quantityStr == null) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Quantity wajib diisi"));
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            boolean updated = itemService.updateStock(id, quantity);

            if (updated) {
                ctx.json(Map.of(
                        "success", true,
                        "message", "Stok berhasil diperbarui"));
            } else {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Gagal memperbarui stok"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Format quantity tidak valid"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/items/:id/update-stock - Quick update stock (add/reduce/set)
     */
    public void quickUpdateStock(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            String action = ctx.bodyAsClass(Map.class).get("action").toString();
            int quantity = Integer.parseInt(ctx.bodyAsClass(Map.class).get("quantity").toString());

            // Get current item
            Item item = itemService.getItemById(id);
            if (item == null) {
                ctx.status(404).json(Map.of(
                        "success", false,
                        "message", "Produk tidak ditemukan"));
                return;
            }

            int newStock;
            switch (action) {
                case "add":
                    newStock = item.getStock() + quantity;
                    break;
                case "reduce":
                case "remove":
                    newStock = Math.max(0, item.getStock() - quantity);
                    break;
                case "set":
                    newStock = quantity;
                    break;
                default:
                    ctx.status(400).json(Map.of(
                            "success", false,
                            "message", "Action tidak valid. Gunakan: add, reduce, atau set"));
                    return;
            }

            boolean updated = itemService.updateStock(id, newStock);

            if (updated) {
                ctx.json(Map.of(
                        "success", true,
                        "message",
                        String.format("Stok berhasil diupdate dari %d menjadi %d unit", item.getStock(), newStock)));
            } else {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Gagal mengupdate stok"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Format quantity tidak valid"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/items/:id/cancel-qc - Cancel QC status
     */
    public void cancelQC(Context ctx) {
        try {
            String id = ctx.pathParam("id");

            // Get current item
            Item item = itemService.getItemById(id);
            if (item == null) {
                ctx.status(404).json(Map.of(
                        "success", false,
                        "message", "Produk tidak ditemukan"));
                return;
            }

            // Only allow cancel if status is NEEDS_QC
            if (!"NEEDS_QC".equals(item.getEligibilityStatus())) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Hanya produk dengan status 'Perlu QC' yang dapat dibatalkan"));
                return;
            }

            // Change status back to ELIGIBLE
            boolean updated = itemService.updateEligibilityStatus(id, "ELIGIBLE");

            if (updated) {
                ctx.json(Map.of(
                        "success", true,
                        "message", "Status QC berhasil dibatalkan, produk dikembalikan ke status 'Layak Jual'"));
            } else {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Gagal membatalkan QC"));
            }
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * PATCH /api/admin/items/:id/stock - Update item stock (API)
     * Note: This is the old endpoint kept for backward compatibility
     */
    public void updateItemStockOld(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            String quantityStr = ctx.formParam("quantity");

            if (quantityStr == null) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Quantity wajib diisi"));
                return;
            }

            int quantity = Integer.parseInt(quantityStr);
            boolean updated = itemService.updateStock(id, quantity);

            if (updated) {
                ctx.json(Map.of(
                        "success", true,
                        "message", "Stok berhasil diperbarui"));
            } else {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Gagal memperbarui stok"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Format quantity tidak valid"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/items/:id - Delete item (API)
     */
    public void deleteItem(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            itemService.deleteItem(id);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Item berhasil dihapus"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/items/export-csv - Export items to CSV.
     */
    public void exportItemsCsv(Context ctx) {
        try {
            List<Item> items = itemService.getAllItems();
            Map<String, String> categoryMap = categoryService.getAllCategories().stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));

            String header = "id,name,category_id,category_name,price,stock,condition,eligibility_status,is_published,description,image_url";
            String rows = items.stream()
                    .map(item -> String.join(",",
                            CsvUtils.escape(item.getId()),
                            CsvUtils.escape(item.getName()),
                            CsvUtils.escape(item.getCategoryId()),
                            CsvUtils.escape(categoryMap.getOrDefault(item.getCategoryId(), "")),
                            CsvUtils.escape(item.getPrice() != null ? item.getPrice().toPlainString() : ""),
                            CsvUtils.escape(item.getStock() != null ? item.getStock().toString() : "0"),
                            CsvUtils.escape(item.getCondition()),
                            CsvUtils.escape(item.getEligibilityStatus()),
                            CsvUtils.escape(item.getIsPublished() != null ? item.getIsPublished().toString() : "false"),
                            CsvUtils.escape(item.getDescription()),
                            CsvUtils.escape(item.getImageUrl())))
                    .collect(Collectors.joining("\n"));

            String csv = header + "\n" + rows;
            ctx.contentType("text/csv");
            ctx.header("Content-Disposition", "attachment; filename=items.csv");
            ctx.result(csv);
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/items/import-csv - Import items from CSV.
     */
    public void importItemsCsv(Context ctx) {
        try {
            UploadedFile file = ctx.uploadedFile("file");
            if (file == null || file.size() == 0) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "File CSV tidak ditemukan"));
                return;
            }

            List<List<String>> rows = CsvUtils.parse(file.content());
            if (rows.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "File CSV kosong"));
                return;
            }

            Map<String, String> categoryNameMap = categoryService.getAllCategories().stream()
                    .collect(Collectors.toMap(cat -> cat.getName().toLowerCase(), Category::getId, (a, b) -> a));

            Map<String, Integer> headerIndex = CsvUtils.buildHeaderIndex(rows.get(0));
            int imported = 0;
            int updated = 0;
            int skipped = 0;

            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                String id = CsvUtils.getCell(row, headerIndex, "id").trim();
                String name = CsvUtils.getCell(row, headerIndex, "name").trim();
                String categoryId = CsvUtils.getCell(row, headerIndex, "category_id").trim();
                String categoryName = CsvUtils.getCell(row, headerIndex, "category_name", "category").trim();
                String priceValue = CsvUtils.getCell(row, headerIndex, "price").trim();
                String stockValue = CsvUtils.getCell(row, headerIndex, "stock").trim();
                String condition = CsvUtils.getCell(row, headerIndex, "condition").trim();
                String eligibilityStatus = CsvUtils.getCell(row, headerIndex, "eligibility_status", "status").trim();
                String isPublishedValue = CsvUtils.getCell(row, headerIndex, "is_published").trim();
                String description = CsvUtils.getCell(row, headerIndex, "description");
                String imageUrl = CsvUtils.getCell(row, headerIndex, "image_url");

                if (name.isEmpty() || priceValue.isEmpty() || stockValue.isEmpty() || condition.isEmpty()) {
                    skipped++;
                    continue;
                }

                if (categoryId.isEmpty() && !categoryName.isEmpty()) {
                    categoryId = categoryNameMap.getOrDefault(categoryName.toLowerCase(), "");
                }

                if (categoryId.isEmpty()) {
                    skipped++;
                    continue;
                }

                BigDecimal price;
                int stock;
                try {
                    price = new BigDecimal(priceValue);
                    stock = Integer.parseInt(stockValue);
                } catch (NumberFormatException e) {
                    skipped++;
                    continue;
                }

                if (eligibilityStatus.isEmpty()) {
                    eligibilityStatus = "ELIGIBLE";
                }

                if (!eligibilityStatus.equals("ELIGIBLE") && !eligibilityStatus.equals("NEEDS_QC")
                        && !eligibilityStatus.equals("NEEDS_REPAIR")) {
                    skipped++;
                    continue;
                }

                Boolean isPublished = null;
                if (!isPublishedValue.isEmpty()) {
                    isPublished = Boolean.parseBoolean(isPublishedValue);
                } else {
                    isPublished = "ELIGIBLE".equals(eligibilityStatus);
                }

                boolean exists = false;
                if (!id.isEmpty()) {
                    try {
                        itemService.getItemById(id);
                        exists = true;
                    } catch (RuntimeException ignored) {
                        exists = false;
                    }
                }

                try {
                    itemService.upsertItemFromImport(id.isEmpty() ? null : id, categoryId, name, condition,
                            description, price, stock, eligibilityStatus, isPublished,
                            imageUrl.isEmpty() ? null : imageUrl);
                    if (exists) {
                        updated++;
                    } else {
                        imported++;
                    }
                } catch (RuntimeException e) {
                    skipped++;
                }
            }

            ctx.json(Map.of(
                    "success", true,
                    "imported", imported,
                    "updated", updated,
                    "skipped", skipped,
                    "message", "Import produk selesai"));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Helper method to save uploaded image file
     * 
     * @param uploadedFile The uploaded file from the request
     * @return The URL path to access the saved image
     */
    private String saveUploadedImage(UploadedFile uploadedFile) throws Exception {
        // Create uploads directory if it doesn't exist
        String uploadDir = "src/main/resources/static/images/products";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Generate unique filename
        String originalName = uploadedFile.filename();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex);
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save the file
        File destFile = new File(dir, uniqueFilename);
        try (InputStream is = uploadedFile.content();
                FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        // Return the URL path
        return "/images/products/" + uniqueFilename;
    }
}
