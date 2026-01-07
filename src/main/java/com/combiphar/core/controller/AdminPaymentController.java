package com.combiphar.core.controller;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.combiphar.core.model.Order;
import com.combiphar.core.model.Payment;
import com.combiphar.core.repository.OrderRepository;
import com.combiphar.core.repository.PaymentRepository;
import com.combiphar.core.util.CustomerUtil;
import com.combiphar.core.util.Pagination;

import io.javalin.http.Context;

/**
 * Controller untuk admin payment verification.
 */
public class AdminPaymentController extends BaseAdminController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm");
    private static final Map<String, String> STATUS_BADGES = Map.of(
            "PENDING", "<span class=\"badge badge--warning\">Menunggu Konfirmasi</span>",
            "SUCCESS", "<span class=\"badge badge--success\">Diterima</span>",
            "FAILED", "<span class=\"badge badge--danger\">Ditolak</span>");

    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final OrderRepository orderRepository = new OrderRepository();

    public void showPaymentPage(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);

        List<Map<String, Object>> paymentDetails = orderRepository.findAll().stream()
                .map(order -> paymentRepository.findByOrderId(order.getId())
                .map(payment -> buildPaymentDetail(order, payment)).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toList());

        Pagination<Map<String, Object>> pagination = new Pagination<>(paymentDetails, page, 25);
        Map<String, Object> model = buildBaseModel(ctx);
        model.put("title", "Verifikasi Pembayaran");
        model.put("pageTitle", "Verifikasi Pembayaran");
        model.put("activePage", "payments");
        model.put("payments", pagination.getItems());
        model.put("currentPage", pagination.getCurrentPage());
        model.put("totalPages", pagination.getTotalPages());
        model.put("hasNext", pagination.hasNext());
        model.put("hasPrevious", pagination.hasPrevious());
        model.put("stats", calculateStats(paymentDetails));
        ctx.render("admin/payment", model);
    }

    public void showPaymentProof(Context ctx) {
        Payment payment = paymentRepository.findByOrderId(ctx.pathParam("id"))
                .orElseThrow(() -> new IllegalArgumentException("Payment tidak ditemukan"));

        if (payment.getProofFilePath() == null) {
            ctx.status(404).result("Bukti pembayaran tidak tersedia");
            return;
        }

        Map<String, Object> model = buildBaseModel(ctx);
        model.put("title", "Bukti Pembayaran");
        model.put("proofImagePath", payment.getProofFilePath());
        model.put("isPdf", payment.getProofFilePath().toLowerCase().endsWith(".pdf"));
        ctx.render("admin/payment-proof", model);
    }

    public void verifyPayment(Context ctx) {
        String orderId = ctx.pathParam("id");
        String action = ctx.queryParam("action");

        if (action == null || (!action.equals("approve") && !action.equals("reject"))) {
            ctx.status(400).json(Map.of("success", false, "message", "Invalid action"));
            return;
        }

        try {
            String paymentStatus = action.equals("approve") ? "SUCCESS" : "FAILED";
            paymentRepository.updateStatus(orderId, paymentStatus);

            // Update order payment status dan order status
            if (action.equals("approve")) {
                orderRepository.updatePaymentStatus(orderId, "PAID");
                orderRepository.updateOrderStatus(orderId, "PROCESSING");
            } else {
                orderRepository.updatePaymentStatus(orderId, "FAILED");
            }

            ctx.json(Map.of("success", true, "message", "Pembayaran berhasil di" + (action.equals("approve") ? "terima" : "tolak")));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private Map<String, Object> buildPaymentDetail(Order order, Payment payment) {
        String proofPath = payment.getProofFilePath();
        boolean isPdf = proofPath != null && proofPath.toLowerCase().endsWith(".pdf");

        return Map.of(
                "order", order,
                "payment", payment,
                "invoiceNumber", order.getOrderNumber(),
                "customerName", CustomerUtil.getCustomerName(order.getUserId()),
                "formattedAmount", formatCurrency(payment.getAmount()),
                "paymentMethod", payment.getBank() != null ? "Transfer " + payment.getBank() : "Transfer",
                "statusBadge", STATUS_BADGES.getOrDefault(payment.getStatus(), "<span class=\"badge\">Unknown</span>"),
                "formattedDate", payment.getPaidAt() != null ? payment.getPaidAt().format(DATE_FMT)
                : (payment.getCreatedAt() != null ? payment.getCreatedAt().format(DATE_FMT) : "-"),
                "isPdf", isPdf);
    }

    private Map<String, Integer> calculateStats(List<Map<String, Object>> details) {
        Map<String, Integer> stats = new HashMap<>(Map.of("pending", 0, "success", 0, "failed", 0, "total", details.size()));
        details.forEach(d -> stats.merge(((Payment) d.get("payment")).getStatus().toLowerCase(), 1, Integer::sum));
        return stats;
    }

    private String formatCurrency(BigDecimal amount) {
        return amount == null ? "Rp 0" : String.format("Rp %,.0f", amount);
    }
}
