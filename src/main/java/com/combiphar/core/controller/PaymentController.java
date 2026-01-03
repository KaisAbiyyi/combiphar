package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.service.OrderService;
import com.combiphar.core.service.PaymentService;

import io.javalin.http.Context;

/**
 * Controller untuk halaman pembayaran transfer.
 */
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = Objects.requireNonNull(paymentService);
        this.orderService = Objects.requireNonNull(orderService);
    }

    public void showPaymentPage(Context ctx) {
        Cart cart = ctx.sessionAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            ctx.redirect("/cart?error=empty");
            return;
        }

        Map<String, Object> model = buildModel(ctx, cart);
        model.put("title", "Pembayaran Transfer");
        model.put("allBankAccounts", paymentService.getAvailableBankAccounts());
        ctx.render("customer/payment-transfer", model);
    }

    public void showUploadPage(Context ctx) {
        Cart cart = ctx.sessionAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            ctx.redirect("/cart?error=empty");
            return;
        }

        Map<String, Object> model = buildModel(ctx, cart);
        model.put("title", "Upload Bukti Pembayaran");
        ctx.render("customer/payment-upload", model);
    }

    private Map<String, Object> buildModel(Context ctx, Cart cart) {
        OrderSummary summary = ctx.sessionAttribute("orderSummary");
        if (summary == null) {
            summary = orderService.calculateOrderSummary(cart, ctx.sessionAttribute("selectedCourier"));
            ctx.sessionAttribute("orderSummary", summary);
        }

        Map<String, Object> model = new HashMap<>();
        model.put("currentUser", ctx.sessionAttribute("currentUser"));
        model.put("activePage", "payment");
        model.put("cart", cart);
        model.put("orderSummary", summary);
        model.put("bankAccount", paymentService.getPrimaryBankAccount());
        return model;
    }
}
