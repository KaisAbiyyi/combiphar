package com.combiphar.core.controller;

import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.CartRepository;
import com.combiphar.core.service.CartService;

import io.javalin.http.Context;

/**
 * Controller for managing shopping cart operations. Follows MVC pattern -
 * handles HTTP layer for cart.
 */
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;
    private static final String SESSION_USER = "currentUser";
    private static final String SESSION_CART = "cart";

    public CartController(CartService cartService, CartRepository cartRepository) {
        this.cartService = Objects.requireNonNull(cartService, "CartService tidak boleh null");
        this.cartRepository = Objects.requireNonNull(cartRepository, "CartRepository tidak boleh null");
    }

    /**
     * GET /cart - Displays the shopping cart page.
     */
    public void showCart(Context ctx) {
        User user = ctx.sessionAttribute(SESSION_USER);
        Cart cart = getOrCreateCart(ctx);

        ctx.render("customer/cart", Map.of(
                "title", "Keranjang Belanja",
                "currentUser", user,
                "activePage", "cart",
                "cart", cart
        ));
    }

    /**
     * POST /api/cart/add - Adds an item to the cart.
     */
    public void addToCart(Context ctx) {
        try {
            String itemId = ctx.formParam("itemId");
            Integer quantity = ctx.formParamAsClass("quantity", Integer.class).getOrDefault(1);

            Cart cart = getOrCreateCart(ctx);
            cartService.addToCart(cart, itemId, quantity);
            persistCartIfAuthenticated(ctx, cart);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Produk berhasil ditambahkan ke keranjang",
                    "cartItemCount", cart.getItemCount()
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * PUT /api/cart/update - Updates cart item quantity.
     */
    public void updateCartItem(Context ctx) {
        try {
            String itemId = ctx.formParam("itemId");
            Integer quantity = ctx.formParamAsClass("quantity", Integer.class)
                    .check(q -> q > 0, "Kuantitas harus lebih dari 0")
                    .get();

            Cart cart = getOrCreateCart(ctx);
            cartService.updateCartItemQuantity(cart, itemId, quantity);
            persistCartIfAuthenticated(ctx, cart);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Kuantitas berhasil diperbarui",
                    "cartTotal", cart.getTotalPrice(),
                    "cartItemCount", cart.getItemCount()
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * DELETE /api/cart/remove - Removes an item from cart.
     */
    public void removeFromCart(Context ctx) {
        try {
            String itemId = ctx.formParam("itemId");

            Cart cart = getOrCreateCart(ctx);
            cartService.removeFromCart(cart, itemId);
            persistCartIfAuthenticated(ctx, cart);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Produk berhasil dihapus dari keranjang",
                    "cartItemCount", cart.getItemCount()
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/cart/clear - Clears all items from cart.
     */
    public void clearCart(Context ctx) {
        try {
            Cart cart = getOrCreateCart(ctx);
            cart.clear();
            persistCartIfAuthenticated(ctx, cart);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Keranjang berhasil dikosongkan",
                    "cartItemCount", cart.getItemCount()
            ));
        } catch (IllegalStateException e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Helper method to get existing cart or create new one. Defensive: ensures
     * cart always exists in session.
     */
    private Cart getOrCreateCart(Context ctx) {
        Cart cart = ctx.sessionAttribute(SESSION_CART);
        if (cart == null) {
            User user = ctx.sessionAttribute(SESSION_USER);
            if (user != null) {
                try {
                    cartRepository.findByUserId(user.getId()).ifPresent(loaded -> {
                        ctx.sessionAttribute(SESSION_CART, loaded);
                    });
                } catch (Exception e) {
                    System.err.println("[CartController] failed to load persisted cart: " + e.getMessage());
                }
                cart = ctx.sessionAttribute(SESSION_CART);
            }
            if (cart == null) {
                cart = new Cart();
                ctx.sessionAttribute(SESSION_CART, cart);
            }
        }
        return cart;
    }

    private void persistCartIfAuthenticated(Context ctx, Cart cart) {
        User user = ctx.sessionAttribute(SESSION_USER);
        if (user == null) {
            return;
        }
        try {
            cartRepository.saveCartForUser(user.getId(), cart);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal menyimpan keranjang ke database", e);
        }
    }
}
