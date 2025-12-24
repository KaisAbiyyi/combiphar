package com.combiphar.core.middleware;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;

import io.javalin.http.ForbiddenResponse;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.RedirectResponse;

/**
 * Middleware for Role-Based Access Control (RBAC).
 */
public class AuthMiddleware {

    /**
     * Ensures the user is logged in.
     */
    public static Handler authenticated = ctx -> {
        if (ctx.sessionAttribute("currentUser") == null) {
            throw new RedirectResponse(HttpStatus.FOUND, "/login");
        }
    };

    /**
     * Ensures the user has ADMIN or OWNER role.
     */
    public static Handler adminOnly = ctx -> {
        User user = ctx.sessionAttribute("currentUser");
        String path = ctx.path().replaceAll("/$", ""); // Remove trailing slash for comparison

        // If accessing admin login page
        if (path.equals("/admin/login")) {
            if (user != null) {
                if (user.getRole() == Role.ADMIN || user.getRole() == Role.OWNER) {
                    throw new RedirectResponse(HttpStatus.FOUND, "/admin/dashboard");
                } else {
                    // Customer trying to access admin login -> redirect to home
                    throw new RedirectResponse(HttpStatus.FOUND, "/");
                }
            }
            return;
        }

        // For all other admin routes
        if (user == null) {
            throw new RedirectResponse(HttpStatus.FOUND, "/admin/login");
        } else if (user.getRole() != Role.ADMIN && user.getRole() != Role.OWNER) {
            // Customer trying to access admin routes -> redirect to home
            throw new RedirectResponse(HttpStatus.FOUND, "/");
        }
    };
}
