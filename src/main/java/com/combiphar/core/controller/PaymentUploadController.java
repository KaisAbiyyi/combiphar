package com.combiphar.core.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.PaymentProof;
import com.combiphar.core.service.FileUploadService;
import com.combiphar.core.service.OrderService;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

/**
 * Controller untuk menangani upload bukti pembayaran. Single Responsibility:
 * hanya menangani upload file.
 */
public class PaymentUploadController {

    private final FileUploadService fileUploadService;
    private final OrderService orderService;

    /**
     * Membuat PaymentUploadController baru.
     *
     * @param fileUploadService service untuk upload file
     * @param orderService service untuk order
     */
    public PaymentUploadController(FileUploadService fileUploadService, OrderService orderService) {
        this.fileUploadService = Objects.requireNonNull(fileUploadService);
        this.orderService = Objects.requireNonNull(orderService);
    }

    /**
     * POST /api/payment/upload - Mengunggah bukti pembayaran.
     */
    public void uploadPaymentProof(Context ctx) {
        try {
            UploadedFile uploadedFile = ctx.uploadedFile("paymentProof");
            String bank = ctx.formParam("bank");

            if (uploadedFile == null || bank == null || bank.isBlank()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", uploadedFile == null ? "File tidak ditemukan" : "Pilih bank terlebih dahulu"
                ));
                return;
            }

            PaymentProof proof = saveUploadedFile(uploadedFile);
            String orderId = ctx.sessionAttribute("orderId");

            if (orderId != null) {
                orderService.updatePaymentProof(orderId, proof.getFilePath(), bank);

                // Clear cart setelah payment berhasil
                ctx.sessionAttribute("cart", null);
                ctx.sessionAttribute("orderId", null);
                ctx.sessionAttribute("orderSummary", null);
            }

            // Simpan info ke session untuk tracking
            ctx.sessionAttribute("paymentProof", proof);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Bukti pembayaran berhasil diunggah",
                    "fileName", proof.getFileName(),
                    "uploadedAt", proof.getUploadedAt().toString()
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IOException e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Gagal menyimpan file: " + e.getMessage()
            ));
        }
    }

    private PaymentProof saveUploadedFile(UploadedFile uploadedFile) throws IOException {
        try (InputStream inputStream = uploadedFile.content()) {
            return fileUploadService.saveFile(
                    inputStream,
                    uploadedFile.filename(),
                    uploadedFile.contentType(),
                    uploadedFile.size()
            );
        }
    }
}
