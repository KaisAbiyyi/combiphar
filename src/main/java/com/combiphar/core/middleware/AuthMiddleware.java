package com.combiphar.core.middleware;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;

import io.javalin.http.Handler;

/**
 * Middleware for Role-Based Access Control (RBAC).
 */
public class AuthMiddleware {

    /**
     * Ensures the user is logged in.
     */
    public static Handler authenticated = ctx -> {
        if (ctx.sessionAttribute("currentUser") == null) {
            ctx.redirect("/profile");
        }
    };

    /**
     * Ensures the user has ADMIN or OWNER role.
     */
    public static Handler adminOnly = ctx -> {
        // Skip middleware for login page to avoid redirect loop
        if (ctx.path().equals("/admin/login")) {
            return;
        }

        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.redirect("/admin/login");
        } else if (user.getRole() != Role.ADMIN && user.getRole() != Role.OWNER) {
            ctx.status(403).result("Forbidden: Admin access required");
        }
    };
}
