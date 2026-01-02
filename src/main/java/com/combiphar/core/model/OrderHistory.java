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
        String paymentStatus = order.getStatusPayment();
        String orderStatus = order.getStatusOrder();

        // Badge untuk status pembayaran pending
        if ("PENDING".equals(paymentStatus)) {
            return "pending";
        }

        // Badge untuk status pengiriman jika ada
        if (shipment != null && "PAID".equals(paymentStatus)) {
            switch (shipment.getStatus()) {
                case RECEIVED:
                case DELIVERED:
                    return "selesai";
                case SHIPPED:
                case PROCESSING:
                    return "processing";
                case PENDING:
                    return "pending";
            }
        }

        // Badge untuk status order
        switch (orderStatus) {
            case "NEW":
            case "PROCESSING":
                return "pending";
            case "READY":
            case "COMPLETED":
                return "selesai";
            case "CANCELLED":
                return "cancelled";
            default:
                return "pending";
        }
    }

    public String getStatusText() {
        String paymentStatus = order.getStatusPayment();
        String orderStatus = order.getStatusOrder();

        if ("PENDING".equals(paymentStatus)) {
            return "Menunggu Pembayaran";
        }

        // Prioritas status pengiriman jika ada
        if (shipment != null && "PAID".equals(paymentStatus)) {
            switch (shipment.getStatus()) {
                case RECEIVED:
                    return "Pesanan Selesai";
                case DELIVERED:
                    return "Paket Terkirim";
                case SHIPPED:
                    return "Dalam Perjalanan";
                case PROCESSING:
                    return "Diproses";
                case PENDING:
                    return "Menunggu Konfirmasi";
            }
        }

        switch (orderStatus) {
            case "PENDING":
                return "Menunggu Konfirmasi";
            case "NEW":
            case "PROCESSING":
                return "Diproses";
            case "READY":
                return "Siap Dikirim";
            case "COMPLETED":
                return "Selesai";
            case "CANCELLED":
                return "Dibatalkan";
            default:
                return orderStatus;
        }
    }

    public String getFormattedPrice() {
        return formatCurrency(order.getTotalPrice());
    }

    public String getActionButton() {
        String paymentStatus = order.getStatusPayment();
        String orderStatus = order.getStatusOrder();

        if ("PENDING".equals(paymentStatus)) {
            return "Bayar Sekarang";
        }

        switch (orderStatus) {
            case "COMPLETED":
                return "Beri Penilaian";
            case "CANCELLED":
                return "Pesan Lagi";
            default:
                return "Lihat Detail";
        }
    }

    public String getOrderId() {
        return order.getId();
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("Rp %,d", amount.longValue()).replace(",", ".");
    }
}
