package com.combiphar.core.seeder;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.combiphar.core.model.Cart;
import com.combiphar.core.model.CartItem;
import com.combiphar.core.repository.CartRepository;

/**
 * Simple demo to verify cart persistence. Run with `./gradlew demoCart`.
 */
public class CartDemoSeeder {

    public static void main(String[] args) {
        String demoUser = "demo-user-001";
        Cart cart = new Cart();
        cart.addItem(new CartItem("qc-demo-001", "Demo Item 1", new BigDecimal("1200000"), 2));
        cart.addItem(new CartItem("qc-demo-002", "Demo Item 2", new BigDecimal("2800000"), 1));

        CartRepository repo = new CartRepository();
        try {
            repo.saveCartForUser(demoUser, cart);
            System.out.println("Saved cart for user: " + demoUser);

            repo.findByUserId(demoUser).ifPresentOrElse(loaded -> {
                System.out.println("Loaded cart with itemCount=" + loaded.getItemCount() + " total=" + loaded.getTotalPrice());
            }, () -> {
                System.err.println("Failed to load cart for user: " + demoUser);
            });
        } catch (SQLException e) {
            System.err.println("DB error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
