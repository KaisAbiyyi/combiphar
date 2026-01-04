package com.combiphar.core.middleware;

import java.util.Map;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;

import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import io.javalin.http.RedirectResponse;

/**
 * Middleware for Role-Based Access Control (RBAC). Ensures only authorized
 * users can access specific routes.
 */
public class AuthMiddleware {

    private static final String SESSION_USER = "currentUser";

    /**
     * Middleware to ensure the user is authenticated. Redirects to login if no
     * session is found.
     */
    public static Handler authenticated = ctx -> {
        if (ctx.sessionAttribute(SESSION_USER) == null) {
            throw new RedirectResponse(HttpStatus.FOUND, "/login");
        }
    };

    /**
     * Middleware to ensure the user is authenticated for API calls.
     * Returns JSON response with redirect URL instead of throwing RedirectResponse.
     * Use this for AJAX/API endpoints that need to handle unauthenticated state.
     */
    public static Handler authenticatedApi = ctx -> {
        if (ctx.sessionAttribute(SESSION_USER) == null) {
            ctx.status(HttpStatus.UNAUTHORIZED)
               .json(Map.of(
                   "success", false,
                   "authenticated", false,
                   "redirect", "/login"
               ));
            // Stop execution - don't proceed to the controller
            ctx.skipRemainingHandlers();
        }
    };

    /**
     * Middleware to ensure the user has ADMIN privileges. Handles
     * redirection based on the user's role and target path.
     */
    public static Handler adminOnly = ctx -> {
        User user = ctx.sessionAttribute(SESSION_USER);
        String path = ctx.path().replaceAll("/$", "");

        // Special handling for admin login page to prevent infinite loops
        if (path.equals("/admin/login")) {
            handleAdminLoginRedirect(ctx, user);
            return;
        }

        // General admin route protection
        if (user == null) {
            throw new RedirectResponse(HttpStatus.FOUND, "/admin/login");
        }

        if (user.getRole() != Role.ADMIN) {
            throw new RedirectResponse(HttpStatus.FOUND, "/");
        }
    };

    /**
     * Helper to handle redirects when an already logged-in user hits
     * /admin/login.
     */
    private static void handleAdminLoginRedirect(io.javalin.http.Context ctx, User user) {
        if (user != null) {
            if (user.getRole() == Role.ADMIN) {
                throw new RedirectResponse(HttpStatus.FOUND, "/admin/dashboard");
            } else {
                throw new RedirectResponse(HttpStatus.FOUND, "/");
            }
        }
    }
}
