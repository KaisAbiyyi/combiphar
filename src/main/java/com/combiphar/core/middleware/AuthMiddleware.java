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
            ctx.redirect("/login");
        }
    };

    /**
     * Ensures the user has ADMIN or OWNER role.
     */
    public static Handler adminOnly = ctx -> {
        User user = ctx.sessionAttribute("currentUser");

        // If accessing admin login page
        if (ctx.path().equals("/admin/login")) {
            if (user != null) {
                if (user.getRole() == Role.ADMIN || user.getRole() == Role.OWNER) {
                    ctx.redirect("/admin/dashboard");
                } else {
                    // Customer trying to access admin login
                    ctx.status(403).result("Forbidden: Admin access required");
                }
            }
            return;
        }

        // For all other admin routes
        if (user == null) {
            ctx.redirect("/admin/login");
        } else if (user.getRole() != Role.ADMIN && user.getRole() != Role.OWNER) {
            ctx.status(403).result("Forbidden: Admin access required");
        }
    };
}
