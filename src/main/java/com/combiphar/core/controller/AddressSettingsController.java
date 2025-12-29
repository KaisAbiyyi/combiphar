package com.combiphar.core.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.combiphar.core.model.AddressSettings;
import com.combiphar.core.model.ShippingAddress;
import com.combiphar.core.model.User;

import io.javalin.http.Context;

/**
 * Controller for address settings page. Stores data in session (no DB).
 */
/**
 * AddressSettingsController has been removed from active routes. The class
 * remains as a safe stub to avoid compile-time errors if any remaining
 * references exist elsewhere in the codebase.
 */
public final class AddressSettingsController {

    private AddressSettingsController() {
        // intentionally uninstantiable
    }

    public static void disabledEndpoint(io.javalin.http.Context ctx) {
        // Respond with 410 Gone for any accidental calls
        ctx.status(410).json(java.util.Map.of(
                "success", false,
                "message", "Halaman pengaturan alamat telah dihapus"
        ));
    }
}
