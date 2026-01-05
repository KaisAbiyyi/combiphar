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

        // Get year parameter from query string, default to 2025
        String yearParam = ctx.queryParam("year");
        int year = 2025; // default
        if (yearParam != null) {
            try {
                year = Integer.parseInt(yearParam);
                // Validate year is reasonable (between 2020 and 2030)
                if (year < 2020 || year > 2030) {
                    year = 2025;
                }
            } catch (NumberFormatException e) {
                year = 2025;
            }
        }
        model.put("selectedYear", year);

        // Get dashboard statistics
        Map<String, Object> dashboardStats = reportService.getDashboardStats();
        model.putAll(dashboardStats);

        // Get top categories data
        List<Map<String, Object>> topCategories = reportService.getTopCategories(4);
        model.put("topCategories", topCategories);

        // Get monthly revenue data for chart with selected year
        List<Map<String, Object>> monthlyRevenue = reportService.getMonthlyRevenue(year);
        model.put("monthlyRevenue", monthlyRevenue);

        ctx.render("admin/laporan", model);
    }
}
