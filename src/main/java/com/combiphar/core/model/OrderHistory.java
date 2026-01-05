package com.combiphar.core.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * DTO untuk menampilkan order history dengan detail items.
 */
public class OrderHistory {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Map<Shipment.Status, String> SHIPMENT_BADGE = Map.of(
            Shipment.Status.RECEIVED, "selesai", Shipment.Status.DELIVERED, "selesai",
            Shipment.Status.SHIPPED, "processing", Shipment.Status.PROCESSING, "processing");
    private static final Map<Shipment.Status, String> SHIPMENT_TEXT = Map.of(
            Shipment.Status.RECEIVED, "Pesanan Selesai", Shipment.Status.DELIVERED, "Paket Terkirim",
            Shipment.Status.SHIPPED, "Dalam Perjalanan", Shipment.Status.PROCESSING, "Diproses");
    private static final Map<String, String> ORDER_BADGE = Map.of(
            "READY", "selesai", "COMPLETED", "selesai", "CANCELLED", "cancelled");
    private static final Map<String, String> ORDER_TEXT = Map.of(
            "NEW", "Diproses", "PROCESSING", "Diproses", "READY", "Siap Dikirim",
            "COMPLETED", "Selesai", "CANCELLED", "Dibatalkan");

    private final Order order;
    private final List<OrderItem> items;
    private final String firstItemName;
    private final Shipment shipment;

    public OrderHistory(Order order, List<OrderItem> items, String firstItemName) {
        this(order, items, firstItemName, null);
    }

    public OrderHistory(Order order, List<OrderItem> items, String firstItemName, Shipment shipment) {
        this.order = order;
        this.items = items;
        this.firstItemName = firstItemName;
        this.shipment = shipment;
    }

    public String getOrderNumber() {
        return order.getOrderNumber();
    }

    public String getFormattedDate() {
        return order.getCreatedAt().format(DATE_FMT);
    }

    public String getFirstItemName() {
        return firstItemName != null ? firstItemName : "Produk tidak ditemukan";
    }

    public String getItemCount() {
        return items.size() > 1 ? " (+" + (items.size() - 1) + " item)" : "";
    }

    public String getStatusBadge() {
        String paymentStatus = order.getStatusPayment();
        if ("PENDING".equals(paymentStatus)) {
            return "pending";
        }
        if ("FAILED".equals(paymentStatus)) {
            return "failed";
        }
        if (shipment != null && "PAID".equals(paymentStatus)) {
            return SHIPMENT_BADGE.getOrDefault(shipment.getStatus(), "pending");
        }
        return ORDER_BADGE.getOrDefault(order.getStatusOrder(), "pending");
    }

    public String getStatusText() {
        String paymentStatus = order.getStatusPayment();
        if ("PENDING".equals(paymentStatus)) {
            return "Menunggu Konfirmasi";
        }
        if ("FAILED".equals(paymentStatus)) {
            return "Ditolak";
        }
        if (shipment != null && "PAID".equals(paymentStatus)) {
            return SHIPMENT_TEXT.getOrDefault(shipment.getStatus(), "Menunggu Konfirmasi");
        }
        return ORDER_TEXT.getOrDefault(order.getStatusOrder(), order.getStatusOrder());
    }

    public String getFormattedPrice() {
        return formatCurrency(order.getTotalPrice());
    }

    public String getActionButton() {
        return "Detail Order";
    }

    public String getOrderId() {
        return order.getId();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("Rp %,d", amount.longValue()).replace(",", ".");
    }
}
