package com.combiphar.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Model untuk Shipment (pengiriman). Immutable setelah dibuat untuk keamanan
 * data.
 */
public class Shipment {

    /**
     * Status pengiriman yang valid.
     */
    public enum Status {
        PENDING("Menunggu"),
        PROCESSING("Diproses"),
        SHIPPED("Dikirim"),
        DELIVERED("Terkirim"),
        RECEIVED("Diterima");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String id;
    private final String orderId;
    private final String courierName;
    private final String trackingNumber;
    private final Status status;
    private final LocalDateTime shippedAt;
    private final LocalDateTime deliveredAt;
    private final LocalDateTime createdAt;

    /**
     * Constructor untuk membuat shipment baru.
     *
     * @param orderId ID order yang dikirim
     * @param courierName nama kurir
     */
    public Shipment(String orderId, String courierName) {
        this.id = UUID.randomUUID().toString();
        this.orderId = Objects.requireNonNull(orderId, "Order ID wajib diisi");
        this.courierName = courierName;
        this.trackingNumber = null;
        this.status = Status.PENDING;
        this.shippedAt = null;
        this.deliveredAt = null;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor untuk load dari database.
     */
    public Shipment(String id, String orderId, String courierName,
            String trackingNumber, Status status,
            LocalDateTime shippedAt, LocalDateTime deliveredAt,
            LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.courierName = courierName;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCourierName() {
        return courierName;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Cek apakah sudah memiliki nomor resi.
     */
    public boolean hasTrackingNumber() {
        return trackingNumber != null && !trackingNumber.isBlank();
    }
}
