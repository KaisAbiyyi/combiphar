package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.combiphar.core.model.Item;
import com.combiphar.core.model.User;
import com.combiphar.core.service.QualityCheckService;
import com.combiphar.core.service.QualityCheckService.QCSummary;

import io.javalin.http.Context;

/**
 * Controller for Quality Control routes.
 */
public class QualityCheckController {

    private final QualityCheckService qcService;

    public QualityCheckController() {
        this.qcService = new QualityCheckService();
    }

    /**
     * GET /admin/qc - Shows QC dashboard page
     */
    public void showQCDashboard(Context ctx) {
        User currentUser = ctx.sessionAttribute("currentUser");

        if (currentUser == null) {
            ctx.redirect("/admin/login");
            return;
        }

        try {
            List<Item> qcPipeline = qcService.getTodayQCPipeline();
            List<Item> needsRepair = qcService.getItemsNeedingRepair();
            List<Item> eligible = qcService.getEligibleItems();
            QCSummary summary = qcService.getDailyQCSummary();

            Map<String, Object> model = new HashMap<>();
            model.put("title", "Quality Control Dashboard");
            model.put("currentUser", currentUser);
            model.put("activePage", "qc");
            model.put("qcPipeline", qcPipeline);
            model.put("needsRepair", needsRepair);
            model.put("eligible", eligible);
            model.put("summary", summary);

            ctx.render("admin/qc-dashboard", model);
        } catch (Exception e) {
            ctx.status(500);
            ctx.result("Error: " + e.getMessage());
        }
    }

    /**
     * GET /api/admin/qc/pipeline - Get today's QC pipeline (API)
     */
    public void getQCPipeline(Context ctx) {
        try {
            List<Item> items = qcService.getTodayQCPipeline();

            ctx.json(Map.of(
                    "success", true,
                    "data", items,
                    "count", items.size()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/qc/needs-repair - Get items needing repair (API)
     */
    public void getItemsNeedingRepair(Context ctx) {
        try {
            List<Item> items = qcService.getItemsNeedingRepair();

            ctx.json(Map.of(
                    "success", true,
                    "data", items,
                    "count", items.size()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/qc/eligible - Get eligible items (API)
     */
    public void getEligibleItems(Context ctx) {
        try {
            List<Item> items = qcService.getEligibleItems();

            ctx.json(Map.of(
                    "success", true,
                    "data", items,
                    "count", items.size()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/qc/statistics - Get QC statistics (API)
     */
    public void getQCStatistics(Context ctx) {
        try {
            Map<String, Long> stats = qcService.getQCStatistics();

            ctx.json(Map.of(
                    "success", true,
                    "data", stats));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/qc/summary - Get daily QC summary (API)
     */
    public void getDailyQCSummary(Context ctx) {
        try {
            QCSummary summary = qcService.getDailyQCSummary();

            ctx.json(Map.of(
                    "success", true,
                    "data", Map.of(
                            "date", summary.getDate().toString(),
                            "pendingQC", summary.getPendingQC(),
                            "approved", summary.getApproved(),
                            "needsRepair", summary.getNeedsRepair(),
                            "total", summary.getTotal())));
        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/qc/check - Perform quality check on an item (API)
     */
    public void performQualityCheck(Context ctx) {
        try {
            String itemId = ctx.formParam("itemId");
            String newStatus = ctx.formParam("status");
            String notes = ctx.formParam("notes");

            if (itemId == null || newStatus == null) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Item ID dan status wajib diisi"));
                return;
            }

            boolean updated = qcService.performQualityCheck(itemId, newStatus, notes);

            if (updated) {
                ctx.json(Map.of(
                        "success", true,
                        "message", "Quality check berhasil dilakukan"));
            } else {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Gagal melakukan quality check"));
            }
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/qc/batch-approve - Batch approve multiple items (API)
     */
    public void batchApprove(Context ctx) {
        try {
            // Get item IDs from form or JSON
            List<String> itemIds = ctx.queryParams("itemIds");

            if (itemIds == null || itemIds.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Item IDs wajib diisi"));
                return;
            }

            int approved = qcService.batchApproveItems(itemIds);

            ctx.json(Map.of(
                    "success", true,
                    "message", approved + " item berhasil disetujui",
                    "approved", approved));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/qc/batch-reject - Batch reject multiple items (API)
     */
    public void batchReject(Context ctx) {
        try {
            // Get item IDs from form or JSON
            List<String> itemIds = ctx.queryParams("itemIds");

            if (itemIds == null || itemIds.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Item IDs wajib diisi"));
                return;
            }

            int rejected = qcService.batchRejectItems(itemIds);

            ctx.json(Map.of(
                    "success", true,
                    "message", rejected + " item ditolak dan perlu perbaikan",
                    "rejected", rejected));
        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
