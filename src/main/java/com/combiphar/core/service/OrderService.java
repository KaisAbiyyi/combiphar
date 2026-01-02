package com.combiphar.core.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.CartItem;
import com.combiphar.core.model.Order;
import com.combiphar.core.model.OrderItem;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.model.Payment;
import com.combiphar.core.model.ShippingAddress;
import com.combiphar.core.repository.OrderItemRepository;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.repository.PaymentRepository;

/**
 * Service for order calculations and management. Follows Single Responsibility
 * Principle.
 */
public class OrderService {

    // Simplified shipping method pricing (in real app, this would come from external API)
    private static final Map<String, BigDecimal> COURIER_RATES = new HashMap<>();

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    static {
        COURIER_RATES.put("Premium Logistics (2-3 hari)", new BigDecimal("15000"));
        COURIER_RATES.put("Standard Logistics (5-7 hari)", new BigDecimal("12000"));
        COURIER_RATES.put("Express Logistics (1 hari)", new BigDecimal("20000"));
    }

    public OrderService() {
        this.orderRepository = new OrderRepository();
        this.orderItemRepository = new OrderItemRepository();
        this.paymentRepository = new PaymentRepository();
    }

    /**
     * Calculates order summary based on cart and shipping selection. Defensive:
     * validates all inputs.
     *
     * @param cart the shopping cart
     * @param courierName the selected courier
     * @return OrderSummary with calculated totals
     */
    public OrderSummary calculateOrderSummary(Cart cart, String courierName) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart tidak boleh null");
        }
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cart masih kosong");
        }

        BigDecimal subtotal = cart.getTotalPrice();
        BigDecimal shippingCost = getShippingCost(courierName);

        return new OrderSummary(subtotal, shippingCost, courierName);
    }

    /**
     * Gets the shipping cost for a courier. Defensive: returns zero if courier
     * not found.
     *
     * @param courierName the courier name
     * @return the shipping cost
     */
    public BigDecimal getShippingCost(String courierName) {
        if (courierName == null || courierName.isBlank()) {
            return BigDecimal.ZERO;
        }
        return COURIER_RATES.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(courierName.trim()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Returns available courier options with their rates.
     *
     * @return map of courier names to their rates
     */
    public Map<String, BigDecimal> getAvailableCouriers() {
        return new HashMap<>(COURIER_RATES);
    }

    /**
     * Validates shipping address.
     *
     * @param address the shipping address to validate
     * @throws IllegalArgumentException if address is invalid
     */
    public void validateShippingAddress(ShippingAddress address) {
        Objects.requireNonNull(address, "Alamat pengiriman tidak boleh null");
        // ShippingAddress already validates its fields in constructor
    }

    /**
     * Membuat order baru dan menyimpan ke database.
     *
     * @param userId ID user yang melakukan order
     * @param cart keranjang belanja
     * @param courierName nama kurir yang dipilih
     * @return Order yang telah dibuat
     */
    public Order createOrder(String userId, Cart cart, String courierName) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart tidak boleh kosong");
        }

        // Hitung total dengan ongkir
        OrderSummary summary = calculateOrderSummary(cart, courierName);

        // Buat order
        Order order = new Order(userId, summary.getTotalPrice(), courierName);

        // Simpan order ke database
        orderRepository.save(order);

        // Simpan order items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    order.getId(),
                    cartItem.getItemId(),
                    cartItem.getQuantity(),
                    cartItem.getItemPrice()
            );
            orderItemRepository.save(orderItem);
        }

        // Buat payment record dengan bank null (akan dipilih saat pembayaran)
        Payment payment = new Payment(order.getId(), summary.getTotalPrice(), null);
        paymentRepository.save(payment);

        return order;
    }

    /**
     * Update payment dengan bukti pembayaran dan bank.
     */
    public void updatePaymentProof(String orderId, String proofFilePath, String bank) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment tidak ditemukan"));

        paymentRepository.updatePayment(payment.getId(), proofFilePath, bank);
        orderRepository.updatePaymentStatus(orderId, "PAID");
    }

    /**
     * Get order history untuk user.
     */
    public java.util.List<com.combiphar.core.model.OrderHistory> getOrderHistory(String userId) {
        return getOrderHistory(userId, null);
    }

    /**
     * Get order history dengan opsi untuk include shipment data.
     *
     * @param userId ID user
     * @param shippingService optional shipping service untuk ambil data
     * shipment
     * @return list of order history
     */
    public java.util.List<com.combiphar.core.model.OrderHistory> getOrderHistory(
            String userId, ShippingService shippingService) {
        java.util.List<Order> orders = orderRepository.findByUserId(userId);
        java.util.List<com.combiphar.core.model.OrderHistory> history = new java.util.ArrayList<>();

        for (Order order : orders) {
            java.util.List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            String firstItemName = orderItemRepository.findFirstItemNameByOrderId(order.getId());

            // Ambil shipment jika shippingService disediakan
            com.combiphar.core.model.Shipment shipment = null;
            if (shippingService != null) {
                shipment = shippingService.getShipmentByOrderId(order.getId()).orElse(null);
            }

            history.add(new com.combiphar.core.model.OrderHistory(order, items, firstItemName, shipment));
        }

        return history;
    }
}
