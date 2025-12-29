package com.combiphar.core.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an order summary with pricing calculations. Immutable value
 * object.
 */
public class OrderSummary {

    private final BigDecimal subtotal;
    private final BigDecimal shippingCost;
    private final BigDecimal totalPrice;
    private final String courierName;

    /**
     * Creates an order summary with automatic total calculation.
     *
     * @param subtotal the subtotal of all items
     * @param shippingCost the shipping cost
     * @param courierName the selected courier name
     */
    public OrderSummary(BigDecimal subtotal, BigDecimal shippingCost, String courierName) {
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal tidak boleh null");
        this.shippingCost = Objects.requireNonNull(shippingCost, "Biaya kirim tidak boleh null");
        this.courierName = courierName != null ? courierName : "Belum dipilih";
        this.totalPrice = subtotal.add(shippingCost);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getCourierName() {
        return courierName;
    }
}
