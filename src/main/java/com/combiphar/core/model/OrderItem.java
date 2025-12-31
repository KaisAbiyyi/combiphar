package com.combiphar.core.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Model untuk OrderItem.
 */
public class OrderItem {

    private final String id;
    private final String orderId;
    private final String itemId;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public OrderItem(String orderId, String itemId, int quantity, BigDecimal unitPrice) {
        this.id = UUID.randomUUID().toString();
        this.orderId = Objects.requireNonNull(orderId);
        this.itemId = Objects.requireNonNull(itemId);
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice);
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Constructor untuk load dari database
    public OrderItem(String id, String orderId, String itemId, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public OrderItem(String id, String orderId, String itemId, int quantity,
            BigDecimal unitPrice, BigDecimal subtotal) {
        this.id = id;
        this.orderId = orderId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
