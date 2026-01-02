package com.combiphar.core.controller;

import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;
import com.combiphar.core.service.AuthService;

import io.javalin.http.Context;

/**
 * Controller for handling authentication-related HTTP requests. Follows MVC
 * pattern by acting as the glue between View and Service.
 */
public class AuthController {

    private final AuthService authService;
    private static final String SESSION_USER = "currentUser";

    public AuthController(AuthService authService) {
        this.authService = Objects.requireNonNull(authService, "AuthService cannot be null");
    }

    /**
     * GET /login - Displays the customer login page.
     */
    public void showLogin(Context ctx) {
        if (ctx.sessionAttribute(SESSION_USER) != null) {
            ctx.redirect("/profile");
            return;
        }
        ctx.render("customer/login");
    }

    /**
     * POST /login - Processes customer login.
     */
    public void handleLogin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        authService.login(email, password).ifPresentOrElse(user -> {
            ctx.sessionAttribute(SESSION_USER, user);
            ctx.redirect("/profile");
        }, () -> {
            ctx.render("customer/login", Map.of("error", "Email atau password salah"));
        });
    }

    /**
     * GET /register - Displays the registration page.
     */
    public void showRegister(Context ctx) {
        if (ctx.sessionAttribute(SESSION_USER) != null) {
            ctx.redirect("/profile");
            return;
        }
        ctx.render("customer/register");
    }

    /**
     * POST /register - Processes customer registration.
     */
    public void handleRegister(Context ctx) {
        String name = ctx.formParam("name");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try {
            authService.registerCustomer(name, email, password);
            ctx.redirect("/login?registered=true");
        } catch (IllegalArgumentException e) {
            ctx.render("customer/register", Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /admin/login - Displays the admin login page.
     */
    public void showAdminLogin(Context ctx) {
        if (ctx.sessionAttribute(SESSION_USER) != null) {
            ctx.redirect("/admin/dashboard");
            return;
        }
        ctx.render("admin/login");
    }

    /**
     * POST /admin/login - Processes admin login.
     */
    public void handleAdminLogin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        authService.login(email, password)
                .filter(user -> user.getRole() == Role.ADMIN)
                .ifPresentOrElse(user -> {
                    ctx.sessionAttribute(SESSION_USER, user);
                    ctx.redirect("/admin/dashboard");
                }, () -> {
                    ctx.render("admin/login", Map.of("error", "Akses ditolak atau kredensial salah"));
                });
    }

    /**
     * GET /profile - Displays the user profile page.
     */
    public void showProfile(Context ctx) {
        User user = ctx.sessionAttribute(SESSION_USER);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        ctx.render("customer/profile", Map.of(
                "title", "Profil Saya",
                "currentUser", user,
                "activePage", "profile"
        ));
    }

    /**
     * GET /logout - Clears session and redirects to home.
     */
    public void handleLogout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }
}
