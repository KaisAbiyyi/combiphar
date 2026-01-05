package com.combiphar.core.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.combiphar.core.service.DashboardService;

import io.javalin.http.Context;

/**
 * Controller for admin dashboard page.
 */
public class DashboardController extends BaseAdminController {

    private final DashboardService dashboardService;

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

    /**
     * Show admin dashboard with real statistics
     */
    public void showDashboard(Context ctx) {
        Map<String, Object> model = buildBaseModel(ctx);

        // Page metadata
        model.put("pageTitle", "Dashboard Admin");
        model.put("activePage", "dashboard");
        model.put("activeMenu", "Dashboard Admin");

        // Get dashboard statistics
        Map<String, Object> dashboardStats = dashboardService.getDashboardStats();
        model.putAll(dashboardStats);

        // Get recent transactions
        List<Map<String, Object>> recentTransactions = dashboardService.getRecentTransactions();
        model.put("recentTransactions", recentTransactions);

        // Last update timestamp
        String lastUpdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        model.put("lastUpdate", lastUpdate);

        ctx.render("admin/dashboard", model);
    }
}
