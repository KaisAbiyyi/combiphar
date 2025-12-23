package com.combiphar.core.controller;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;
import com.combiphar.core.service.AuthService;
import io.javalin.http.Context;
import java.util.Map;

/**
 * Controller for authentication routes.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * GET /profile - Shows login/register page or user profile.
     */
    public void showProfile(Context ctx) {
        User currentUser = ctx.sessionAttribute("currentUser");
        if (currentUser != null) {
            ctx.render("customer/profile", Map.of(
                "title", "Profil Saya",
                "user", currentUser,
                "activePage", "profile"
            ));
        } else {
            ctx.render("customer/profile", Map.of(
                "title", "Login / Register",
                "activePage", "profile"
            ));
        }
    }

    /**
     * POST /login - Handles customer login.
     */
    public void handleLogin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        authService.login(email, password).ifPresentOrElse(user -> {
            ctx.sessionAttribute("currentUser", user);
            ctx.redirect("/profile");
        }, () -> {
            ctx.render("customer/profile", Map.of(
                "title", "Login / Register",
                "error", "Email atau password salah",
                "activePage", "profile"
            ));
        });
    }

    /**
     * POST /register - Handles customer registration.
     */
    public void handleRegister(Context ctx) {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try {
            authService.registerCustomer(name, email, password);
            ctx.redirect("/profile?registered=true");
        } catch (IllegalArgumentException e) {
            ctx.render("customer/profile", Map.of(
                "title", "Login / Register",
                "error", e.getMessage(),
                "activePage", "profile"
            ));
        }
    }

    /**
     * GET /admin/login - Shows admin login page.
     */
    public void showAdminLogin(Context ctx) {
        ctx.render("admin/login");
    }

    /**
     * POST /admin/login - Handles admin login.
     */
    public void handleAdminLogin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        authService.login(email, password)
            .filter(user -> user.getRole() == Role.ADMIN || user.getRole() == Role.OWNER)
            .ifPresentOrElse(user -> {
                ctx.sessionAttribute("currentUser", user);
                ctx.redirect("/admin/dashboard");
            }, () -> {
                ctx.render("admin/login", Map.of("error", "Akses ditolak atau kredensial salah"));
            });
    }

    /**
     * GET /logout - Handles logout.
     */
    public void handleLogout(Context ctx) {
        ctx.consumeSessionAttribute("currentUser");
        ctx.redirect("/");
    }
}
