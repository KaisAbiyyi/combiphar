package com.combiphar.core.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.model.ShippingAddress;
import com.combiphar.core.model.User;
import com.combiphar.core.service.OrderService;

import io.javalin.http.Context;

/**
 * Controller for checkout and order placement. Follows MVC pattern - handles
 * HTTP layer for checkout.
 */
public class CheckoutController {

    private final OrderService orderService;
    private static final String SESSION_USER = "currentUser";
    private static final String SESSION_CART = "cart";
    private static final String SESSION_SHIPPING = "shippingAddress";

    public CheckoutController(OrderService orderService) {
        this.orderService = Objects.requireNonNull(orderService, "OrderService tidak boleh null");
    }

    /**
     * GET /checkout - Displays the checkout page.
     */
    public void showCheckout(Context ctx) {
        User user = ctx.sessionAttribute(SESSION_USER);
        Cart cart = ctx.sessionAttribute(SESSION_CART);

        if (cart == null || cart.isEmpty()) {
            ctx.redirect("/cart?error=empty");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        Map<String, BigDecimal> couriers = orderService.getAvailableCouriers();
        String defaultCourier = resolveDefaultCourier(couriers);
        OrderSummary summary = orderService.calculateOrderSummary(cart, defaultCourier);

        model.put("title", "Checkout Pesanan");
        model.put("currentUser", user);
        model.put("activePage", "checkout");
        model.put("cart", cart);
        model.put("courierOptions", buildCourierOptions(couriers));
        model.put("selectedCourier", defaultCourier);
        model.put("orderSummary", summary);
        model.put("shippingAddress", ctx.sessionAttribute(SESSION_SHIPPING));
        model.put("addressSettings", ctx.sessionAttribute("addressSettings"));

        ctx.render("customer/checkout", model);
    }

    /**
     * POST /api/checkout/calculate - Calculates order summary.
     */
    public void calculateOrder(Context ctx) {
        try {
            Cart cart = ctx.sessionAttribute(SESSION_CART);
            if (cart == null || cart.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Keranjang belanja kosong"
                ));
                return;
            }

            String courierName = ctx.formParam("courier");
            OrderSummary summary = orderService.calculateOrderSummary(cart, courierName);

            ctx.json(Map.of(
                    "success", true,
                    "subtotal", summary.getSubtotal(),
                    "shippingCost", summary.getShippingCost(),
                    "totalPrice", summary.getTotalPrice(),
                    "courierName", summary.getCourierName()
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/checkout/validate-address - Validates shipping address.
     */
    public void validateAddress(Context ctx) {
        try {
            String recipientName = ctx.formParam("recipientName");
            String address = ctx.formParam("address");
            String city = ctx.formParam("city");
            String postalCode = ctx.formParam("postalCode");
            String phone = ctx.formParam("phone");

            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("Nomor telepon wajib diisi");
            }

            ShippingAddress shippingAddress = new ShippingAddress(
                    recipientName, address, city, postalCode, phone
            );

            orderService.validateShippingAddress(shippingAddress);
            ctx.sessionAttribute(SESSION_SHIPPING, shippingAddress);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Alamat pengiriman valid",
                    "formattedAddress", shippingAddress.getFormattedAddress()
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /checkout/summary - Shows order summary before payment.
     */
    public void showOrderSummary(Context ctx) {
        User user = ctx.sessionAttribute(SESSION_USER);
        Cart cart = ctx.sessionAttribute(SESSION_CART);
        ShippingAddress shippingAddress = ctx.sessionAttribute(SESSION_SHIPPING);

        if (cart == null || cart.isEmpty()) {
            ctx.redirect("/cart?error=empty");
            return;
        }

        if (shippingAddress == null) {
            ctx.redirect("/checkout?error=no_address");
            return;
        }

        String courierName = ctx.queryParam("courier");
        OrderSummary summary = orderService.calculateOrderSummary(cart, courierName);

        Map<String, Object> model = new HashMap<>();
        model.put("title", "Ringkasan Pesanan");
        model.put("currentUser", user);
        model.put("activePage", "checkout");
        model.put("cart", cart);
        model.put("shippingAddress", shippingAddress);
        model.put("orderSummary", summary);

        ctx.render("customer/order-summary", model);
    }

    private String resolveDefaultCourier(Map<String, BigDecimal> couriers) {
        if (couriers == null || couriers.isEmpty()) {
            return "";
        }
        if (couriers.containsKey("Premium Logistics (2-3 hari)")) {
            return "Premium Logistics (2-3 hari)";
        }
        return couriers.keySet().iterator().next();
    }

    private List<Map<String, Object>> buildCourierOptions(Map<String, BigDecimal> couriers) {
        if (couriers == null || couriers.isEmpty()) {
            return List.of();
        }
        return couriers.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> option = new HashMap<>();
                    option.put("name", entry.getKey());
                    option.put("rate", entry.getValue());
                    return option;
                })
                .collect(Collectors.toList());
    }
}
