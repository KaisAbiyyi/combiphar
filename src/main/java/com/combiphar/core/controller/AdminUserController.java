package com.combiphar.core.controller;

import java.util.List;
import java.util.Map;

import com.combiphar.core.model.User;
import com.combiphar.core.repository.UserRepository;
import com.combiphar.core.util.Pagination;

import io.javalin.http.Context;

/**
 * Controller for Admin User Management.
 */
public class AdminUserController extends BaseAdminController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Shows the user management page with filters.
     */
    public void showUsers(Context ctx) {
        // Get filter parameters
        String roleFilter = ctx.queryParam("role");
        if (roleFilter == null) roleFilter = "all";

        String statusFilter = ctx.queryParam("status");
        if (statusFilter == null) statusFilter = "all";

        String searchQuery = ctx.queryParam("search");
        if (searchQuery == null) searchQuery = "";

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);

        // Get filtered users
        List<User> users = userRepository.findAll(roleFilter, statusFilter, searchQuery);

        // Apply pagination
        Pagination<User> pagination = new Pagination<>(users, page, 25);

        // Get statistics
        int totalUsers = userRepository.countTotal();
        int customerCount = userRepository.countByRole("CUSTOMER");
        int adminCount = userRepository.countByRole("ADMIN");
        int activeCount = userRepository.countByStatus("ACTIVE");

        // Build model
        Map<String, Object> model = buildBaseModel(ctx);
        model.put("pageTitle", "Manajemen Pengguna");
        model.put("activePage", "users");
        model.put("users", pagination.getItems());
        model.put("totalUsers", totalUsers);
        model.put("customerCount", customerCount);
        model.put("adminCount", adminCount);
        model.put("activeCount", activeCount);
        model.put("roleFilter", roleFilter);
        model.put("statusFilter", statusFilter);
        model.put("searchQuery", searchQuery);
        model.put("currentPage", pagination.getCurrentPage());
        model.put("totalPages", pagination.getTotalPages());
        model.put("hasNext", pagination.hasNext());
        model.put("hasPrevious", pagination.hasPrevious());

        ctx.render("admin/user", model);
    }

    /**
     * Updates user status.
     */
    public void updateStatus(Context ctx) {
        String userId = ctx.formParam("userId");
        String status = ctx.formParam("status");

        if (userId != null && status != null) {
            userRepository.updateStatus(userId, status);
            ctx.sessionAttribute("successMessage", "Status pengguna berhasil diperbarui");
        }

        ctx.redirect("/admin/users");
    }
}
