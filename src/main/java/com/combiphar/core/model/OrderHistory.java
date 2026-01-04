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
            Shipment.Status status = shipment.getStatus();
            switch (status) {
                case RECEIVED:
                case DELIVERED:
                    return "selesai";
                case SHIPPED:
                case PROCESSING:
                    return "processing";
                default:
                    return "pending";
            }
        }
        String statusOrder = order.getStatusOrder();
        switch (statusOrder) {
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
        if ("PENDING".equals(order.getStatusPayment())) {
            return "Menunggu Pembayaran";
        }
        if (shipment != null && "PAID".equals(order.getStatusPayment())) {
            Shipment.Status status = shipment.getStatus();
            switch (status) {
                case RECEIVED:
                    return "Pesanan Selesai";
                case DELIVERED:
                    return "Paket Terkirim";
                case SHIPPED:
                    return "Dalam Perjalanan";
                case PROCESSING:
                    return "Diproses";
                default:
                    return "Menunggu Konfirmasi";
            }
        }
        String statusOrder = order.getStatusOrder();
        switch (statusOrder) {
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
                return statusOrder;
        }
    }

    public String getFormattedPrice() {
        return formatCurrency(order.getTotalPrice());
    }

    public String getActionButton() {
        if ("PENDING".equals(order.getStatusPayment())) {
            return "Bayar Sekarang";
        }
        String statusOrder = order.getStatusOrder();
        switch (statusOrder) {
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
