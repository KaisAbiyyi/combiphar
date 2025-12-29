package com.combiphar.core.service;

import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.CartItem;
import com.combiphar.core.model.Item;
import com.combiphar.core.repository.ItemRepository;

/**
 * Service for managing shopping cart operations. Follows Single Responsibility
 * Principle.
 */
public class CartService {

    private final ItemRepository itemRepository;

    public CartService(ItemRepository itemRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "ItemRepository tidak boleh null");
    }

    /**
     * Adds an item to the cart with validation. Defensive: validates item
     * exists and has sufficient stock.
     *
     * @param cart the shopping cart
     * @param itemId the item ID to add
     * @param quantity the quantity to add
     * @throws IllegalArgumentException if item not found or insufficient stock
     */
    public void addToCart(Cart cart, String itemId, int quantity) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart tidak boleh null");
        }
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID tidak boleh kosong");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0");
        }

        Item item = findItemOrThrow(itemId);
        validateStock(item, quantity);
        validateEligibility(item);

        CartItem cartItem = new CartItem(
                item.getId(),
                item.getName(),
                item.getPrice(),
                quantity
        );

        cart.addItem(cartItem);
    }

    /**
     * Updates the quantity of a cart item.
     *
     * @param cart the shopping cart
     * @param itemId the item ID
     * @param quantity the new quantity
     */
    public void updateCartItemQuantity(Cart cart, String itemId, int quantity) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart tidak boleh null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0");
        }

        Item item = findItemOrThrow(itemId);
        validateStock(item, quantity);

        cart.updateQuantity(itemId, quantity);
    }

    /**
     * Removes an item from the cart.
     *
     * @param cart the shopping cart
     * @param itemId the item ID to remove
     */
    public void removeFromCart(Cart cart, String itemId) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart tidak boleh null");
        }
        cart.removeItem(itemId);
    }

    /**
     * Finds an item by ID or throws exception. Defensive: fail fast if item not
     * found.
     */
    private Item findItemOrThrow(String itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan"));
    }

    /**
     * Validates item has sufficient stock.
     */
    private void validateStock(Item item, int requestedQuantity) {
        if (item.getStock() == null || item.getStock() < requestedQuantity) {
            throw new IllegalArgumentException("Stok tidak mencukupi. Stok tersedia: "
                    + (item.getStock() != null ? item.getStock() : 0));
        }
    }

    /**
     * Validates item is eligible for sale.
     */
    private void validateEligibility(Item item) {
        if (!"ELIGIBLE".equalsIgnoreCase(item.getEligibilityStatus())) {
            throw new IllegalArgumentException("Produk tidak tersedia untuk dijual");
        }
        if (item.getIsPublished() == null || !item.getIsPublished()) {
            throw new IllegalArgumentException("Produk tidak dipublikasikan");
        }
    }
}
