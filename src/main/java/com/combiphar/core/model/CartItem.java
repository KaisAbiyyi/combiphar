package com.combiphar.core.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an item in the shopping cart. Immutable value object following
 * defensive programming principles.
 */
public class CartItem {

    private final String itemId;
    private final String itemName;
    private final BigDecimal itemPrice;
    private final int quantity;
    private final String imageUrl;

    /**
     * Creates a cart item with validation.
     *
     * @param itemId the product ID
     * @param itemName the product name
     * @param itemPrice the product price
     * @param quantity the quantity (must be positive)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public CartItem(String itemId, String itemName, BigDecimal itemPrice, int quantity) {
        this(itemId, itemName, itemPrice, quantity, null);
    }

    /**
     * Creates a cart item with validation including image URL.
     *
     * @param itemId the product ID
     * @param itemName the product name
     * @param itemPrice the product price
     * @param quantity the quantity (must be positive)
     * @param imageUrl the product image URL (optional)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public CartItem(String itemId, String itemName, BigDecimal itemPrice, int quantity, String imageUrl) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID tidak boleh kosong");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("Nama item tidak boleh kosong");
        }
        if (itemPrice == null || itemPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Harga harus positif");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Kuantitas harus lebih dari 0");
        }

        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Calculates the subtotal for this cart item.
     *
     * @return the item price multiplied by quantity
     */
    public BigDecimal getSubtotal() {
        return itemPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Creates a new CartItem with updated quantity. Defensive: returns new
     * instance instead of mutating.
     *
     * @param newQuantity the new quantity
     * @return a new CartItem with the updated quantity
     */
    public CartItem withQuantity(int newQuantity) {
        return new CartItem(itemId, itemName, itemPrice, newQuantity, imageUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CartItem cartItem = (CartItem) o;
        return Objects.equals(itemId, cartItem.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
}
