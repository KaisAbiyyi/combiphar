package com.combiphar.core.controller;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Address;
import com.combiphar.core.model.Cart;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.model.ShippingAddress;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.AddressRepository;
import com.combiphar.core.service.OrderService;

import io.javalin.http.Context;

/**
 * Controller for checkout and order placement.
 */
public class CheckoutController {

    private final OrderService orderService;
    private final AddressRepository addressRepository;

    public CheckoutController(OrderService orderService, AddressRepository addressRepository) {
        this.orderService = Objects.requireNonNull(orderService);
        this.addressRepository = Objects.requireNonNull(addressRepository);
    }

    public void showCheckout(Context ctx) {
        Cart cart = ctx.sessionAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            ctx.redirect("/cart?error=empty");
            return;
        }

        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        Map<String, BigDecimal> couriers = orderService.getAvailableCouriers();
        String defaultCourier = couriers.containsKey("Premium Logistics (2-3 hari)")
                ? "Premium Logistics (2-3 hari)" : couriers.keySet().stream().findFirst().orElse("");

        Map<String, Object> model = new HashMap<>();
        model.put("title", "Checkout Pesanan");
        model.put("currentUser", user);
        model.put("activePage", "checkout");
        model.put("cart", cart);
        model.put("courierOptions", couriers.entrySet().stream()
                .map(e -> Map.of("name", e.getKey(), "rate", e.getValue())).toList());
        model.put("selectedCourier", defaultCourier);
        model.put("orderSummary", orderService.calculateOrderSummary(cart, defaultCourier));
        loadAddresses(model, user.getId());
        ctx.render("customer/checkout", model);
    }

    public void calculateOrder(Context ctx) {
        Cart cart = ctx.sessionAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            ctx.status(400).json(Map.of("success", false, "message", "Keranjang belanja kosong"));
            return;
        }
        try {
            OrderSummary summary = orderService.calculateOrderSummary(cart, ctx.formParam("courier"));
            ctx.json(Map.of("success", true, "subtotal", summary.getSubtotal(),
                    "shippingCost", summary.getShippingCost(), "totalPrice", summary.getTotalPrice(),
                    "courierName", summary.getCourierName()));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public void validateAddress(Context ctx) {
        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        String addressId = ctx.formParam("addressId");
        if (addressId == null || addressId.isBlank()) {
            redirectError(ctx, "/checkout", "Alamat pengiriman wajib dipilih");
            return;
        }

        try {
            Address address = addressRepository.findById(addressId)
                    .filter(a -> a.getUserId().equals(user.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Alamat tidak valid"));

            ctx.sessionAttribute("shippingAddress", new ShippingAddress(
                    address.getRecipientName(), address.getFullAddress(),
                    address.getCity(), address.getPostalCode(), address.getPhone()));
            ctx.sessionAttribute("selectedAddressId", addressId);

            String courier = ctx.formParam("courier");
            if (courier != null && !courier.isBlank()) {
                ctx.sessionAttribute("selectedCourier", courier);
            }
            ctx.redirect("/payment");
        } catch (Exception e) {
            redirectError(ctx, "/checkout", e.getMessage());
        }
    }

    private void loadAddresses(Map<String, Object> model, String userId) {
        try {
            List<Address> addresses = addressRepository.findByUserId(userId);
            model.put("addresses", addresses);
            addressRepository.findPrimaryByUserId(userId).ifPresent(a -> model.put("selectedAddress", a));
        } catch (Exception e) {
            model.put("addresses", List.of());
        }
    }

    private void redirectError(Context ctx, String path, String message) {
        ctx.redirect(path + "?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
