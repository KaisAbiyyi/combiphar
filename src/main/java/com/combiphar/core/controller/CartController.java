package com.combiphar.core.controller;

import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.CartRepository;
import com.combiphar.core.service.CartService;

import io.javalin.http.Context;

/**
 * Controller for shopping cart operations.
 */
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    public CartController(CartService cartService, CartRepository cartRepository) {
        this.cartService = Objects.requireNonNull(cartService);
        this.cartRepository = Objects.requireNonNull(cartRepository);
    }

    public void showCart(Context ctx) {
        ctx.render("customer/cart", Map.of(
                "title", "Keranjang Belanja",
                "currentUser", ctx.sessionAttribute("currentUser"),
                "activePage", "cart",
                "cart", getOrCreateCart(ctx)
        ));
    }

    public void addToCart(Context ctx) {
        handleCartAction(ctx, cart -> {
            cartService.addToCart(cart, ctx.formParam("itemId"),
                    ctx.formParamAsClass("quantity", Integer.class).getOrDefault(1));
            return Map.of("success", true, "message", "Produk berhasil ditambahkan ke keranjang",
                    "cartItemCount", cart.getItemCount());
        });
    }

    public void updateCartItem(Context ctx) {
        handleCartAction(ctx, cart -> {
            cartService.updateCartItemQuantity(cart, ctx.formParam("itemId"),
                    ctx.formParamAsClass("quantity", Integer.class).check(q -> q > 0, "Kuantitas harus > 0").get());
            return Map.of("success", true, "message", "Kuantitas berhasil diperbarui",
                    "cartTotal", cart.getTotalPrice(), "cartItemCount", cart.getItemCount());
        });
    }

    public void removeFromCart(Context ctx) {
        handleCartAction(ctx, cart -> {
            cartService.removeFromCart(cart, ctx.formParam("itemId"));
            return Map.of("success", true, "message", "Produk berhasil dihapus",
                    "cartItemCount", cart.getItemCount());
        });
    }

    public void clearCart(Context ctx) {
        handleCartAction(ctx, cart -> {
            cart.clear();
            return Map.of("success", true, "message", "Keranjang berhasil dikosongkan",
                    "cartItemCount", 0);
        });
    }

    private void handleCartAction(Context ctx, CartAction action) {
        try {
            Cart cart = getOrCreateCart(ctx);
            ctx.json(action.execute(cart));
            persistCart(ctx, cart);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface CartAction {

        Map<String, Object> execute(Cart cart);
    }

    private Cart getOrCreateCart(Context ctx) {
        Cart cart = ctx.sessionAttribute("cart");
        if (cart != null) {
            return cart;
        }

        User user = ctx.sessionAttribute("currentUser");
        if (user != null) {
            try {
                cart = cartRepository.findByUserId(user.getId()).orElse(null);
            } catch (Exception ignored) {
            }
        }
        if (cart == null) {
            cart = new Cart();
        }
        ctx.sessionAttribute("cart", cart);
        return cart;
    }

    private void persistCart(Context ctx, Cart cart) {
        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            return;
        }
        try {
            cartRepository.saveCartForUser(user.getId(), cart);
        } catch (Exception e) {
            throw new IllegalStateException("Gagal menyimpan keranjang", e);
        }
    }
}
