package com.combiphar.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Model untuk Order. Immutable setelah dibuat untuk keamanan data.
 */
public class Order {

    private final String id;
    private final String userId;
    private final String orderNumber;
    private final BigDecimal totalPrice;
    private final String paymentMethod;
    private final String pickupMethod;
    private final String statusPayment;
    private final String statusOrder;
    private final String note;
    private final LocalDateTime createdAt;

    /**
     * Constructor untuk membuat order baru.
     */
    public Order(String userId, BigDecimal totalPrice, String courierName) {
        this.id = UUID.randomUUID().toString();
        this.userId = Objects.requireNonNull(userId, "User ID required");
        this.orderNumber = generateOrderNumber();
        this.totalPrice = Objects.requireNonNull(totalPrice, "Total price required");
        this.paymentMethod = "TRANSFER";
        this.pickupMethod = "DELIVERY";
        this.statusPayment = "PENDING";
        this.statusOrder = "PENDING";
        this.note = courierName;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor untuk load dari database.
     */
    public Order(String id, String userId, String orderNumber, BigDecimal totalPrice,
            String paymentMethod, String pickupMethod, String statusPayment,
            String statusOrder, String note, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.pickupMethod = pickupMethod;
        this.statusPayment = statusPayment;
        this.statusOrder = statusOrder;
        this.note = note;
        this.createdAt = createdAt;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPickupMethod() {
        return pickupMethod;
    }

    public String getStatusPayment() {
        return statusPayment;
    }

    public String getStatusOrder() {
        return statusOrder;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
