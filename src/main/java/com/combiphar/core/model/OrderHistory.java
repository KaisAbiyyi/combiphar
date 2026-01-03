package com.combiphar.core.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DTO untuk menampilkan order history dengan detail items.
 */
public class OrderHistory {

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return order.getCreatedAt().format(formatter);
    }

    public String getFirstItemName() {
        return firstItemName != null ? firstItemName : "Produk tidak ditemukan";
    }

    public String getItemCount() {
        int count = items.size();
        if (count > 1) {
            return " (+" + (count - 1) + " item)";
        }
        return "";
    }

    public String getStatusBadge() {
        if ("PENDING".equals(order.getStatusPayment())) {
            return "pending";
        }
        if (shipment != null && "PAID".equals(order.getStatusPayment())) {
            return switch (shipment.getStatus()) {
                case RECEIVED, DELIVERED ->
                    "selesai";
                case SHIPPED, PROCESSING ->
                    "processing";
                default ->
                    "pending";
            };
        }
        return switch (order.getStatusOrder()) {
            case "READY", "COMPLETED" ->
                "selesai";
            case "CANCELLED" ->
                "cancelled";
            default ->
                "pending";
        };
    }

    public String getStatusText() {
        if ("PENDING".equals(order.getStatusPayment())) {
            return "Menunggu Pembayaran";
        }
        if (shipment != null && "PAID".equals(order.getStatusPayment())) {
            return switch (shipment.getStatus()) {
                case RECEIVED ->
                    "Pesanan Selesai";
                case DELIVERED ->
                    "Paket Terkirim";
                case SHIPPED ->
                    "Dalam Perjalanan";
                case PROCESSING ->
                    "Diproses";
                default ->
                    "Menunggu Konfirmasi";
            };
        }
        return switch (order.getStatusOrder()) {
            case "NEW", "PROCESSING" ->
                "Diproses";
            case "READY" ->
                "Siap Dikirim";
            case "COMPLETED" ->
                "Selesai";
            case "CANCELLED" ->
                "Dibatalkan";
            default ->
                order.getStatusOrder();
        };
    }

    public String getFormattedPrice() {
        return formatCurrency(order.getTotalPrice());
    }

    public String getActionButton() {
        if ("PENDING".equals(order.getStatusPayment())) {
            return "Bayar Sekarang";
        }
        return switch (order.getStatusOrder()) {
            case "COMPLETED" ->
                "Beri Penilaian";
            case "CANCELLED" ->
                "Pesan Lagi";
            default ->
                "Lihat Detail";
        };
    }

    public String getOrderId() {
        return order.getId();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("Rp %,d", amount.longValue()).replace(",", ".");
    }
}
