package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.combiphar.core.service.ReportService;

import io.javalin.http.Context;

/**
 * Controller for admin reports/laporan page.
 * Handles statistics, revenue, and top categories.
 */
public class ReportController extends BaseAdminController {

    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    /**
     * Show reports page with statistics and top categories
     */
    public void showReports(Context ctx) {
        Map<String, Object> model = buildBaseModel(ctx);
        
        // Page metadata
        model.put("pageTitle", "Laporan Kinerja");
        model.put("activePage", "reports");
        model.put("activeMenu", "Laporan Kinerja");
        
        // Get top categories data
        List<Map<String, Object>> topCategories = reportService.getTopCategories(4);
        model.put("topCategories", topCategories);
        
        // Calculate total revenue from top categories
        double totalRevenue = topCategories.stream()
                .mapToDouble(cat -> ((Number) cat.get("totalRevenue")).doubleValue())
                .sum();
        model.put("totalRevenue", totalRevenue);
        
        ctx.render("admin/laporan", model);
    }
}
