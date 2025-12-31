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

            if (uploadedFile == null) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "File bukti pembayaran tidak ditemukan"
                ));
                return;
            }

            PaymentProof proof = saveUploadedFile(uploadedFile);

            // Ambil orderId dari session
            String orderId = ctx.sessionAttribute("orderId");
            if (orderId != null) {
                // Update payment proof di database
                orderService.updatePaymentProof(orderId, proof.getFilePath());

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
