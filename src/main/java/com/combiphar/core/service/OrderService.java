package com.combiphar.core.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.model.ShippingAddress;

/**
 * Service for order calculations and management. Follows Single Responsibility
 * Principle.
 */
public class OrderService {

    // Simplified shipping method pricing (in real app, this would come from external API)
    private static final Map<String, BigDecimal> COURIER_RATES = new HashMap<>();

    static {
        COURIER_RATES.put("Premium Logistics (2-3 hari)", new BigDecimal("15000"));
        COURIER_RATES.put("Standard Logistics (5-7 hari)", new BigDecimal("12000"));
        COURIER_RATES.put("Express Logistics (1 hari)", new BigDecimal("20000"));
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
}
