package com.combiphar.core.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.CartItem;
import com.combiphar.core.model.Order;
import com.combiphar.core.model.OrderHistory;
import com.combiphar.core.model.OrderItem;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.model.Payment;
import com.combiphar.core.repository.OrderItemRepository;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.repository.PaymentRepository;

/**
 * Service for order calculations and management.
 */
public class OrderService {

    private static final Map<String, BigDecimal> COURIER_RATES = Map.of(
            "Premium Logistics (2-3 hari)", new BigDecimal("15000"),
            "Standard Logistics (5-7 hari)", new BigDecimal("12000"),
            "Express Logistics (1 hari)", new BigDecimal("20000"));

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public OrderService() {
        this(new OrderRepository(), new OrderItemRepository(), new PaymentRepository());
    }

    public OrderService(OrderRepository orderRepo, OrderItemRepository itemRepo, PaymentRepository paymentRepo) {
        this.orderRepository = orderRepo;
        this.orderItemRepository = itemRepo;
        this.paymentRepository = paymentRepo;
    }

    public OrderSummary calculateOrderSummary(Cart cart, String courierName) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart tidak boleh kosong");
        }
        return new OrderSummary(cart.getTotalPrice(), getShippingCost(courierName), courierName);
    }

    public BigDecimal getShippingCost(String courierName) {
        if (courierName == null || courierName.isBlank()) {
            return BigDecimal.ZERO;
        }
        return COURIER_RATES.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(courierName.trim()))
                .map(Map.Entry::getValue).findFirst().orElse(BigDecimal.ZERO);
    }

    public Map<String, BigDecimal> getAvailableCouriers() {
        return new HashMap<>(COURIER_RATES);
    }

    public Order createOrder(String userId, String addressId, Cart cart, String courierName, String bank, String proofPath) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart tidak boleh kosong");
        }

        OrderSummary summary = calculateOrderSummary(cart, courierName);
        Order order = new Order(userId, addressId, summary.getTotalPrice(), courierName);
        orderRepository.save(order);

        for (CartItem ci : cart.getItems()) {
            orderItemRepository.save(new OrderItem(order.getId(), ci.getItemId(), ci.getQuantity(), ci.getItemPrice()));
        }

        paymentRepository.save(new Payment(order.getId(), summary.getTotalPrice(), bank, proofPath, "PENDING"));
        return order;
    }

    public List<OrderHistory> getOrderHistory(String userId) {
        return getOrderHistory(userId, null);
    }

    public List<OrderHistory> getOrderHistory(String userId, ShipmentService shipmentService) {
        List<OrderHistory> history = new ArrayList<>();
        for (Order order : orderRepository.findByUserId(userId)) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            String firstItemName = orderItemRepository.findFirstItemNameByOrderId(order.getId());
            var shipment = shipmentService != null ? shipmentService.getShipmentByOrderId(order.getId()).orElse(null) : null;
            history.add(new OrderHistory(order, items, firstItemName, shipment));
        }
        return history;
    }
}
