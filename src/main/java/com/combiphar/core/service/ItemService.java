package com.combiphar.core.service;

import java.math.BigDecimal;
import java.util.List;

import com.combiphar.core.model.Item;
import com.combiphar.core.repository.ItemRepository;
import com.combiphar.core.repository.CategoryRepository;

/**
 * Service for managing items/products.
 */
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemService() {
        this.itemRepository = new ItemRepository();
        this.categoryRepository = new CategoryRepository();
    }

    /**
     * Get all items
     */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * Get item by ID
     */
    public Item getItemById(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan"));
    }

    /**
     * Get items by category
     */
    public List<Item> getItemsByCategory(String categoryId) {
        return itemRepository.findByCategoryId(categoryId);
    }

    /**
     * Get items by eligibility status
     */
    public List<Item> getItemsByEligibilityStatus(String status) {
        return itemRepository.findByEligibilityStatus(status);
    }

    /**
     * Get published items (for customer catalog)
     */
    public List<Item> getPublishedItems() {
        return itemRepository.findPublished();
    }

    /**
     * Search published items with filters for customer catalog
     * 
     * @param searchQuery search term for name (can be null)
     * @param categoryId  filter by category (can be null)
     * @return list of matching published items
     */
    public List<Item> searchPublishedItems(String searchQuery, String categoryId) {
        return itemRepository.searchPublishedItems(searchQuery, categoryId);
    }

    /**
     * Create new item
     */
    public Item createItem(String categoryId, String name, String condition,
            String description, BigDecimal price, Integer stock,
            String eligibilityStatus, Boolean isPublished) {
        return createItem(categoryId, name, condition, description, price, stock, eligibilityStatus, isPublished, null);
    }

    /**
     * Create new item with image
     */
    public Item createItem(String categoryId, String name, String condition,
            String description, BigDecimal price, Integer stock,
            String eligibilityStatus, Boolean isPublished, String imageUrl) {
        // Validate category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        // Validate price and stock
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Harga tidak boleh negatif");
        }
        if (stock < 0) {
            throw new RuntimeException("Stok tidak boleh negatif");
        }

        Item item = new Item();
        item.setCategoryId(categoryId);
        item.setName(name);
        item.setCondition(condition);
        item.setDescription(description);
        item.setPrice(price);
        item.setStock(stock);
        item.setEligibilityStatus(eligibilityStatus != null ? eligibilityStatus : "ELIGIBLE");
        item.setIsPublished(isPublished != null ? isPublished : false);
        item.setImageUrl(imageUrl);

        return itemRepository.save(item);
    }

    /**
     * Update existing item
     */
    public Item updateItem(String id, String categoryId, String name, String condition,
            String description, BigDecimal price, Integer stock,
            String eligibilityStatus, Boolean isPublished) {
        return updateItem(id, categoryId, name, condition, description, price, stock, eligibilityStatus, isPublished,
                null);
    }

    /**
     * Update existing item with image
     */
    public Item updateItem(String id, String categoryId, String name, String condition,
            String description, BigDecimal price, Integer stock,
            String eligibilityStatus, Boolean isPublished, String imageUrl) {
        // Check if item exists
        Item existingItem = getItemById(id);

        // Validate category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        // Validate price and stock
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Harga tidak boleh negatif");
        }
        if (stock < 0) {
            throw new RuntimeException("Stok tidak boleh negatif");
        }

        existingItem.setCategoryId(categoryId);
        existingItem.setName(name);
        existingItem.setCondition(condition);
        existingItem.setDescription(description);
        existingItem.setPrice(price);
        existingItem.setStock(stock);
        existingItem.setEligibilityStatus(eligibilityStatus);
        existingItem.setIsPublished(isPublished);

        // Only update imageUrl if provided
        if (imageUrl != null) {
            existingItem.setImageUrl(imageUrl);
        }

        return itemRepository.update(id, existingItem);
    }

    /**
     * Create or update item based on ID for CSV import.
     */
    public Item upsertItemFromImport(String id, String categoryId, String name, String condition,
            String description, BigDecimal price, Integer stock,
            String eligibilityStatus, Boolean isPublished, String imageUrl) {
        if (id != null && !id.trim().isEmpty()) {
            return itemRepository.findById(id)
                    .map(existing -> updateItem(existing.getId(), categoryId, name, condition, description, price,
                            stock, eligibilityStatus, isPublished, imageUrl))
                    .orElseGet(() -> createItem(categoryId, name, condition, description, price, stock,
                            eligibilityStatus, isPublished, imageUrl));
        }

        return createItem(categoryId, name, condition, description, price, stock, eligibilityStatus, isPublished,
                imageUrl);
    }

    /**
     * Update item eligibility status (for QC)
     */
    public boolean updateEligibilityStatus(String id, String status) {
        // Validate status
        if (!isValidEligibilityStatus(status)) {
            throw new RuntimeException("Status kelayakan tidak valid. Gunakan: ELIGIBLE, NEEDS_QC, atau NEEDS_REPAIR");
        }

        // Check if item exists
        getItemById(id);

        return itemRepository.updateEligibilityStatus(id, status);
    }

    /**
     * Update item stock
     */
    public boolean updateStock(String id, int quantity) {
        if (quantity < 0) {
            throw new RuntimeException("Stok tidak boleh negatif");
        }

        // Check if item exists
        getItemById(id);

        return itemRepository.updateStock(id, quantity);
    }

    /**
     * Delete item
     */
    public void deleteItem(String id) {
        // Check if item exists
        getItemById(id);

        boolean deleted = itemRepository.deleteById(id);
        if (!deleted) {
            throw new RuntimeException("Gagal menghapus item");
        }
    }

    /**
     * Delete all items by category ID
     */
    public int deleteItemsByCategoryId(String categoryId) {
        return itemRepository.deleteByCategoryId(categoryId);
    }

    /**
     * Count items by category ID
     */
    public int countItemsByCategoryId(String categoryId) {
        return itemRepository.countByCategoryId(categoryId);
    }

    /**
     * Validate eligibility status
     */
    private boolean isValidEligibilityStatus(String status) {
        return status.equals("ELIGIBLE") ||
                status.equals("NEEDS_QC") ||
                status.equals("NEEDS_REPAIR");
    }
}
