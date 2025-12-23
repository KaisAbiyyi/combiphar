package com.combiphar.core.controller;

import java.util.Map;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;
import com.combiphar.core.service.AuthService;

import io.javalin.http.Context;

/**
 * Controller for authentication routes.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * GET /profile - Shows user profile.
     */
    public void showProfile(Context ctx) {
        User currentUser = ctx.sessionAttribute("currentUser");
        ctx.render("customer/profile", Map.of(
            "title", "Profil Saya",
            "user", currentUser,
            "activePage", "profile"
        ));
    }

    /**
     * GET /login - Shows customer login page.
     */
    public void showLogin(Context ctx) {
        if (ctx.sessionAttribute("currentUser") != null) {
            ctx.redirect("/profile");
            return;
        }
        ctx.render("customer/login");
    }

    /**
     * GET /register - Shows customer registration page.
     */
    public void showRegister(Context ctx) {
        if (ctx.sessionAttribute("currentUser") != null) {
            ctx.redirect("/profile");
            return;
        }
        ctx.render("customer/register");
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
            ctx.render("customer/login", Map.of(
                "error", "Email atau password salah"
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
            ctx.redirect("/login?registered=true");
        } catch (IllegalArgumentException e) {
            ctx.render("customer/register", Map.of(
                "error", e.getMessage()
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
