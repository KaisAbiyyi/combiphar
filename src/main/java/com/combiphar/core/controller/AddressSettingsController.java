package com.combiphar.core.controller;

/**
 * AddressSettingsController - disabled stub.
 */
public final class AddressSettingsController {

    private AddressSettingsController() {
    }

    public static void disabledEndpoint(io.javalin.http.Context ctx) {
        ctx.status(410).json(java.util.Map.of("success", false, "message", "Halaman pengaturan alamat telah dihapus"));
    }
}
