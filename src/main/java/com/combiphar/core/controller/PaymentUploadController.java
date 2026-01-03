package com.combiphar.core.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.OrderSummary;
import com.combiphar.core.model.PaymentProof;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.CartRepository;
import com.combiphar.core.service.FileUploadService;
import com.combiphar.core.service.OrderService;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

/**
 * Controller untuk upload bukti pembayaran.
 */
public class PaymentUploadController {

    private static final String[] SESSION_KEYS = {"cart", "orderId", "orderSummary", "selectedAddressId", "selectedCourier"};

    private final FileUploadService fileUploadService;
    private final OrderService orderService;
    private final CartRepository cartRepository;

    public PaymentUploadController(FileUploadService fileUploadService, OrderService orderService, CartRepository cartRepository) {
        this.fileUploadService = Objects.requireNonNull(fileUploadService);
        this.orderService = Objects.requireNonNull(orderService);
        this.cartRepository = Objects.requireNonNull(cartRepository);
    }

    public void uploadPaymentProof(Context ctx) {
        try {
            UploadedFile file = ctx.uploadedFile("paymentProof");
            String bank = ctx.formParam("bank");

            if (file == null || bank == null || bank.isBlank()) {
                ctx.status(400).json(Map.of("success", false,
                        "message", file == null ? "File tidak ditemukan" : "Pilih bank terlebih dahulu"));
                return;
            }

            User user = ctx.sessionAttribute("currentUser");
            Cart cart = ctx.sessionAttribute("cart");
            String addressId = ctx.sessionAttribute("selectedAddressId");

            if (user == null || cart == null || cart.isEmpty() || addressId == null) {
                ctx.status(400).json(Map.of("success", false, "message", "Session tidak valid, silakan mulai dari checkout"));
                return;
            }

            PaymentProof proof;
            try (var stream = file.content()) {
                proof = fileUploadService.saveFile(stream, file.filename(), file.contentType(), file.size());
            }

            OrderSummary summary = ctx.sessionAttribute("orderSummary");
            String courier = ctx.sessionAttribute("selectedCourier");
            orderService.createOrder(user.getId(), addressId, cart,
                    courier != null ? courier : (summary != null ? summary.getCourierName() : null), bank, proof.getFilePath());

            // Clear cart dari database
            try {
                cartRepository.clearCartForUser(user.getId());
            } catch (Exception ignored) {
                // Best effort - jika gagal clear DB, cart session tetap di-clear
            }

            for (String key : SESSION_KEYS) {
                ctx.sessionAttribute(key, null);
            }
            ctx.sessionAttribute("paymentProof", proof);

            ctx.json(Map.of("success", true, "message", "Bukti pembayaran berhasil diunggah",
                    "fileName", proof.getFileName(), "uploadedAt", proof.getUploadedAt().toString()));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            ctx.status(500).json(Map.of("success", false, "message", "Gagal menyimpan file: " + e.getMessage()));
        }
    }
}
