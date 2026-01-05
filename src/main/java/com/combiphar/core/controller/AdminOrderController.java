package com.combiphar.core.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.combiphar.core.model.Order;
import com.combiphar.core.repository.ItemRepository;
import com.combiphar.core.repository.OrderItemRepository;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.repository.PaymentRepository;
import com.combiphar.core.util.CustomerUtil;
import com.combiphar.core.util.Pagination;

import io.javalin.http.Context;

/**
 * Controller untuk admin order management. Simple and focused.
 */
public class AdminOrderController extends BaseAdminController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final int PAGE_SIZE = 5;

    private final OrderRepository orderRepo = new OrderRepository();
    private final PaymentRepository paymentRepo = new PaymentRepository();
    private final OrderItemRepository orderItemRepo = new OrderItemRepository();
    private final ItemRepository itemRepo = new ItemRepository();

    public void showOrders(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        List<Order> orders = orderRepo.findAll();

        List<Map<String, Object>> details = orders.stream().map(this::buildDetail).collect(Collectors.toList());
        Pagination<Map<String, Object>> pagination = new Pagination<>(details, page, PAGE_SIZE);

        Map<String, Object> model = buildBaseModel(ctx);
        model.put("title", "Monitoring Pesanan");
        model.put("pageTitle", "Monitoring Pesanan");
        model.put("activePage", "orders");
        model.put("orders", pagination.getItems());
        model.put("currentPage", pagination.getCurrentPage());
        model.put("totalPages", pagination.getTotalPages());
        model.put("hasNext", pagination.hasNext());
        model.put("hasPrevious", pagination.hasPrevious());
        model.put("stats", calculateStats(orders));
        ctx.render("admin/order", model);
    }

    public void getOrderItems(Context ctx) {
        String orderId = ctx.pathParam("id");
        List<Map<String, Object>> items = orderItemRepo.findByOrderId(orderId).stream()
                .map(oi -> itemRepo.findById(oi.getItemId())
                .map(item -> Map.<String, Object>of(
                        "name", item.getName(),
                        "quantity", oi.getQuantity(),
                        "unitPrice", oi.getUnitPrice(),
                        "subtotal", oi.getSubtotal()))
                .orElse(null))
                .filter(m -> m != null)
                .collect(Collectors.toList());
        ctx.json(items);
    }

    private Map<String, Object> buildDetail(Order order) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("order", order);
        detail.put("customerName", CustomerUtil.getCustomerName(order.getUserId()));
        if (order.getCreatedAt() != null) {
            detail.put("formattedDate", order.getCreatedAt().format(DATE_FMT));
        }
        paymentRepo.findByOrderId(order.getId()).ifPresent(p -> detail.put("payment", p));
        return detail;
    }

    private Map<String, Integer> calculateStats(List<Order> orders) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", orders.size());
        stats.put("newOrders", (int) orders.stream().filter(o -> "NEW".equals(o.getStatusOrder())).count());
        stats.put("processing", (int) orders.stream().filter(o -> "PROCESSING".equals(o.getStatusOrder())).count());
        stats.put("completed", (int) orders.stream().filter(o -> "COMPLETED".equals(o.getStatusOrder())).count());
        return stats;
    }
}
