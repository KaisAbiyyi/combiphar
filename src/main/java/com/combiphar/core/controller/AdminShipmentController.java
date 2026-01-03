package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.combiphar.core.model.Order;
import com.combiphar.core.model.Shipment;
import com.combiphar.core.model.Shipment.Status;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.service.ShipmentService;
import com.combiphar.core.util.CustomerUtil;
import com.combiphar.core.util.Pagination;

import io.javalin.http.Context;

/**
 * Controller untuk admin shipment.
 */
public class AdminShipmentController extends BaseAdminController {

    private static final Map<Status, String> STATUS_KEY_MAP = Map.of(
            Status.PROCESSING, "packed", Status.SHIPPED, "shipped",
            Status.DELIVERED, "delivered", Status.RECEIVED, "received");

    private final ShipmentService shipmentService;
    private final OrderRepository orderRepository = new OrderRepository();

    public AdminShipmentController(ShipmentService shipmentService) {
        this.shipmentService = Objects.requireNonNull(shipmentService);
    }

    public void showShipmentPage(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);

        List<Map<String, Object>> shipmentDetails = orderRepository.findAll().stream()
                .filter(o -> "PAID".equals(o.getStatusPayment()))
                .map(this::buildShipmentDetail)
                .collect(Collectors.toList());

        Pagination<Map<String, Object>> pagination = new Pagination<>(shipmentDetails, page, 6);
        Map<String, Object> model = buildBaseModel(ctx);
        model.put("title", "Monitoring Pengiriman");
        model.put("pageTitle", "Monitoring Pengiriman");
        model.put("activePage", "shipment");
        model.put("shipments", pagination.getItems());
        model.put("currentPage", pagination.getCurrentPage());
        model.put("totalPages", pagination.getTotalPages());
        model.put("hasNext", pagination.hasNext());
        model.put("hasPrevious", pagination.hasPrevious());
        model.put("stats", calculateStats(shipmentDetails));
        ctx.render("admin/shipment", model);
    }

    public void updateTrackingNumber(Context ctx) {
        handleAction(ctx, () -> {
            shipmentService.updateTrackingNumber(ctx.pathParam("id"), ctx.formParam("trackingNumber"));
            return "Nomor resi berhasil diupdate";
        });
    }

    public void updateStatus(Context ctx) {
        handleAction(ctx, () -> {
            String status = ctx.formParam("status");
            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException("Status wajib diisi");
            }
            shipmentService.updateStatus(ctx.pathParam("id"), Status.valueOf(status.toUpperCase()));
            return "Status berhasil diupdate";
        });
    }

    public void createShipment(Context ctx) {
        handleAction(ctx, () -> {
            Order order = orderRepository.findById(ctx.formParam("orderId"))
                    .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan"));
            Shipment shipment = shipmentService.createShipment(order.getId(), order.getAddressId(), ctx.formParam("courierName"));
            return Map.of("success", true, "message", "Shipment berhasil dibuat", "shipmentId", shipment.getId());
        });
    }

    private void handleAction(Context ctx, ActionSupplier action) {
        try {
            Object result = action.get();
            ctx.json(result instanceof Map ? result : Map.of("success", true, "message", result));
        } catch (IllegalArgumentException | IllegalStateException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface ActionSupplier {

        Object get();
    }

    private Map<String, Object> buildShipmentDetail(Order order) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("order", order);
        detail.put("customerName", CustomerUtil.getCustomerName(order.getUserId()));
        shipmentService.getShipmentByOrderId(order.getId()).ifPresent(s -> detail.put("shipment", s));
        return detail;
    }

    private Map<String, Integer> calculateStats(List<Map<String, Object>> details) {
        Map<String, Integer> stats = new HashMap<>(Map.of(
                "pending", 0, "packed", 0, "shipped", 0, "delivered", 0, "received", 0));
        details.forEach(d -> {
            Shipment s = (Shipment) d.get("shipment");
            stats.merge(s == null ? "pending" : STATUS_KEY_MAP.getOrDefault(s.getStatus(), "pending"), 1, Integer::sum);
        });
        return stats;
    }
}
