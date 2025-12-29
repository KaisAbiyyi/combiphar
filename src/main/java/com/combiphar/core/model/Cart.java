package com.combiphar.core.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a shopping cart containing items. Uses defensive copying to
 * maintain immutability.
 */
public class Cart {

    private final List<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds or updates an item in the cart. If item exists, updates quantity;
     * otherwise adds new item.
     *
     * @param item the cart item to add
     */
    public void addItem(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cart item tidak boleh null");
        }

        Optional<CartItem> existing = items.stream()
                .filter(i -> i.getItemId().equals(item.getItemId()))
                .findFirst();

        if (existing.isPresent()) {
            items.remove(existing.get());
            int newQuantity = existing.get().getQuantity() + item.getQuantity();
            items.add(item.withQuantity(newQuantity));
        } else {
            items.add(item);
        }
    }

    /**
     * Updates the quantity of an existing item.
     *
     * @param itemId the item ID
     * @param quantity the new quantity
     */
    public void updateQuantity(String itemId, int quantity) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID tidak boleh kosong");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0");
        }

        items.stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    items.remove(item);
                    items.add(item.withQuantity(quantity));
                });
    }

    /**
     * Removes an item from the cart.
     *
     * @param itemId the item ID to remove
     */
    public void removeItem(String itemId) {
        items.removeIf(item -> item.getItemId().equals(itemId));
    }

    /**
     * Returns an immutable view of cart items. Defensive: prevents external
     * modification.
     */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Calculates the total price of all items in the cart.
     *
     * @return the sum of all item subtotals
     */
    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the number of items in the cart.
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Checks if the cart is empty.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Clears all items from the cart.
     */
    public void clear() {
        items.clear();
    }
}
