package com.combiphar.core.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Order;
import com.combiphar.core.model.Shipment;
import com.combiphar.core.model.Shipment.Status;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.service.ShippingService;

import io.javalin.http.Context;

/**
 * Controller untuk halaman admin shipping. Mengikuti pola MVC - hanya menangani
 * HTTP layer.
 */
public class AdminShippingController {

    private final ShippingService shippingService;
    private final OrderRepository orderRepository;

    /**
     * Membuat AdminShippingController baru.
     *
     * @param shippingService service untuk shipping
     */
    public AdminShippingController(ShippingService shippingService) {
        this.shippingService = Objects.requireNonNull(shippingService, "ShippingService required");
        this.orderRepository = new OrderRepository();
    }

    /**
     * GET /admin/shipping - Menampilkan halaman monitoring pengiriman.
     */
    public void showShippingPage(Context ctx) {
        Map<String, Object> model = buildBaseModel(ctx);
        model.put("title", "Monitoring Pengiriman");
        model.put("pageTitle", "Monitoring Pengiriman");
        model.put("activePage", "shipping");

        // Ambil semua orders yang sudah dibayar
        List<Order> allOrders = orderRepository.findAll();
        List<Order> paidOrders = new ArrayList<>();
        for (Order o : allOrders) {
            if ("PAID".equals(o.getStatusPayment())) {
                paidOrders.add(o);
            }
        }

        // Buat detail pengiriman untuk setiap order
        List<Map<String, Object>> shippingDetails = new ArrayList<>();
        for (Order order : paidOrders) {
            shippingDetails.add(buildShippingDetail(order));
        }

        model.put("shipments", shippingDetails);
        model.put("stats", calculateStats(shippingDetails));

        ctx.render("admin/shipping", model);
    }

    /**
     * POST /api/admin/shipping/{id}/tracking - Update nomor resi.
     */
    public void updateTrackingNumber(Context ctx) {
        String shipmentId = ctx.pathParam("id");
        String trackingNumber = ctx.formParam("trackingNumber");

        try {
            shippingService.updateTrackingNumber(shipmentId, trackingNumber);
            ctx.json(Map.of(
                    "success", true,
                    "message", "Nomor resi berhasil diupdate"
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/admin/shipping/{id}/status - Update status pengiriman.
     */
    public void updateStatus(Context ctx) {
        String shipmentId = ctx.pathParam("id");
        String statusStr = ctx.formParam("status");

        try {
            Status status = parseStatus(statusStr);
            shippingService.updateStatus(shipmentId, status);
            ctx.json(Map.of(
                    "success", true,
                    "message", "Status berhasil diupdate"
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/admin/shipping/create - Buat shipment baru untuk order.
     */
    public void createShipment(Context ctx) {
        String orderId = ctx.formParam("orderId");
        String courierName = ctx.formParam("courierName");

        try {
            Shipment shipment = shippingService.createShipment(orderId, courierName);
            ctx.json(Map.of(
                    "success", true,
                    "message", "Shipment berhasil dibuat",
                    "shipmentId", shipment.getId()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // === Private Helper Methods ===
    private Map<String, Object> buildBaseModel(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("currentUser", ctx.sessionAttribute("currentUser"));
        return model;
    }

    private Map<String, Object> buildShippingDetail(Order order) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("order", order);

        // Nama customer diambil dari order note atau default
        detail.put("customerName", "Customer #" + order.getUserId().substring(0, 8));

        // Cari shipment jika ada
        shippingService.getShipmentByOrderId(order.getId())
                .ifPresent(shipment -> detail.put("shipment", shipment));

        return detail;
    }

    private Map<String, Integer> calculateStats(List<Map<String, Object>> details) {
        int pending = 0, packed = 0, shipped = 0, delivered = 0;

        for (Map<String, Object> detail : details) {
            Shipment shipment = (Shipment) detail.get("shipment");
            if (shipment == null) {
                pending++;
            } else {
                switch (shipment.getStatus()) {
                    case PENDING:
                        pending++;
                        break;
                    case PROCESSING:
                        packed++;
                        break;
                    case SHIPPED:
                        shipped++;
                        break;
                    case DELIVERED:
                        delivered++;
                        break;
                }
            }
        }

        Map<String, Integer> stats = new HashMap<>();
        stats.put("pending", pending);
        stats.put("packed", packed);
        stats.put("shipped", shipped);
        stats.put("delivered", delivered);
        return stats;
    }

    private Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status wajib diisi");
        }
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status tidak valid: " + status);
        }
    }
}
