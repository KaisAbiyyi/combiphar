package com.combiphar.core.service;

import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.CartItem;
import com.combiphar.core.model.Item;
import com.combiphar.core.repository.ItemRepository;

/**
 * Service for shopping cart operations.
 */
public class CartService {

    private final ItemRepository itemRepository;

    public CartService(ItemRepository itemRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository);
    }

    public void addToCart(Cart cart, String itemId, int quantity) {
        Objects.requireNonNull(cart, "Cart tidak boleh null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0");
        }

        Item item = findItemOrThrow(itemId);
        validateStock(item, quantity);
        validateEligibility(item);

        cart.addItem(new CartItem(item.getId(), item.getName(), item.getPrice(), quantity));
    }

    public void updateCartItemQuantity(Cart cart, String itemId, int quantity) {
        Objects.requireNonNull(cart, "Cart tidak boleh null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0");
        }
        validateStock(findItemOrThrow(itemId), quantity);
        cart.updateQuantity(itemId, quantity);
    }

    public void removeFromCart(Cart cart, String itemId) {
        Objects.requireNonNull(cart, "Cart tidak boleh null");
        cart.removeItem(itemId);
    }

    private Item findItemOrThrow(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID tidak boleh kosong");
        }
        return itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan"));
    }

    private void validateStock(Item item, int qty) {
        if (item.getStock() == null || item.getStock() < qty) {
            throw new IllegalArgumentException("Stok tidak mencukupi. Tersedia: " + (item.getStock() != null ? item.getStock() : 0));
        }
    }

    private void validateEligibility(Item item) {
        if (!"ELIGIBLE".equalsIgnoreCase(item.getEligibilityStatus())) {
            throw new IllegalArgumentException("Produk tidak tersedia untuk dijual");
        }
        if (item.getIsPublished() == null || !item.getIsPublished()) {
            throw new IllegalArgumentException("Produk tidak dipublikasikan");
        }
    }
}
