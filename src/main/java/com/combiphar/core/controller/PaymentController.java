package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.model.User;
import com.combiphar.core.service.OrderService;
import com.combiphar.core.service.PaymentService;

import io.javalin.http.Context;

/**
 * Controller untuk halaman pembayaran transfer. Mengikuti pola MVC - hanya
 * menangani HTTP layer.
 */
public class PaymentController {

    private static final String SESSION_USER = "currentUser";
    private static final String SESSION_CART = "cart";
    private static final String SESSION_ORDER_SUMMARY = "orderSummary";
    private static final String SESSION_ORDER_ID = "orderId";

    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * Membuat PaymentController baru.
     *
     * @param paymentService service untuk pembayaran
     * @param orderService service untuk order
     */
    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = Objects.requireNonNull(paymentService, "PaymentService required");
        this.orderService = Objects.requireNonNull(orderService, "OrderService required");
    }

    /**
     * GET /payment - Menampilkan halaman pilihan metode pembayaran transfer.
     * Jika belum ada order, akan membuat order baru.
     */
    public void showPaymentPage(Context ctx) {
        User user = ctx.sessionAttribute(SESSION_USER);
        Cart cart = ctx.sessionAttribute(SESSION_CART);

        // Defensive: redirect jika cart kosong
        if (cart == null || cart.isEmpty()) {
            ctx.redirect("/cart?error=empty");
            return;
        }

        // Hitung order summary
        OrderSummary summary = ctx.sessionAttribute(SESSION_ORDER_SUMMARY);
        if (summary == null) {
            summary = orderService.calculateOrderSummary(cart, null);
            ctx.sessionAttribute(SESSION_ORDER_SUMMARY, summary);
        }

        // Buat order jika belum ada
        String orderId = ctx.sessionAttribute(SESSION_ORDER_ID);
        if (orderId == null && user != null) {
            try {
                com.combiphar.core.model.Order order = orderService.createOrder(
                        user.getId(),
                        cart,
                        summary.getCourierName()
                );
                ctx.sessionAttribute(SESSION_ORDER_ID, order.getId());
            } catch (Exception e) {
                System.err.println("Error creating order: " + e.getMessage());
                ctx.redirect("/checkout?error=order_failed");
                return;
            }
        }

        Map<String, Object> model = buildBaseModel(user);
        model.put("title", "Pembayaran Transfer");
        model.put("activePage", "payment");
        model.put("cart", cart);
        model.put("orderSummary", summary);
        model.put("bankAccount", paymentService.getPrimaryBankAccount());
        model.put("allBankAccounts", paymentService.getAvailableBankAccounts());

        ctx.render("customer/payment-transfer", model);
    }

    /**
     * GET /payment/upload - Menampilkan halaman upload bukti pembayaran.
     */
    public void showUploadPage(Context ctx) {
        User user = ctx.sessionAttribute(SESSION_USER);
        Cart cart = ctx.sessionAttribute(SESSION_CART);

        // Defensive: redirect jika cart kosong
        if (cart == null || cart.isEmpty()) {
            ctx.redirect("/cart?error=empty");
            return;
        }

        OrderSummary summary = ctx.sessionAttribute(SESSION_ORDER_SUMMARY);
        if (summary == null) {
            summary = orderService.calculateOrderSummary(cart, null);
        }

        Map<String, Object> model = buildBaseModel(user);
        model.put("title", "Upload Bukti Pembayaran");
        model.put("activePage", "payment");
        model.put("cart", cart);
        model.put("orderSummary", summary);
        model.put("bankAccount", paymentService.getPrimaryBankAccount());

        ctx.render("customer/payment-upload", model);
    }

    private Map<String, Object> buildBaseModel(User user) {
        Map<String, Object> model = new HashMap<>();
        model.put("currentUser", user);
        return model;
    }
}
