package com.combiphar.core.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.combiphar.core.model.Address;
import com.combiphar.core.model.User;
import com.combiphar.core.repository.AddressRepository;

import io.javalin.http.Context;

/**
 * Controller for managing user addresses.
 */
public class AddressController {

    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = Objects.requireNonNull(addressRepository);
    }

    public void showAddressList(Context ctx) {
        User user = getUser(ctx);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        List<Address> addresses;
        String error = ctx.queryParam("error");
        try {
            addresses = addressRepository.findByUserId(user.getId());
        } catch (Exception e) {
            addresses = List.of();
            error = "Gagal memuat daftar alamat";
        }

        ctx.render("customer/address-list", Map.of(
                "title", "Daftar Alamat",
                "currentUser", user,
                "activePage", "addresses",
                "addresses", addresses,
                "success", ctx.queryParam("success") != null ? ctx.queryParam("success") : "",
                "error", error != null ? error : ""));
    }

    public void showAddressForm(Context ctx) {
        User user = getUser(ctx);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        ctx.render("customer/address-form", Map.of(
                "title", "Tambah Alamat",
                "currentUser", user,
                "activePage", "addresses",
                "error", ctx.queryParam("error") != null ? ctx.queryParam("error") : ""));
    }

    public void handleAddAddress(Context ctx) {
        User user = getUser(ctx);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        try {
            addressRepository.save(new Address.Builder()
                    .userId(user.getId())
                    .recipientName(ctx.formParam("recipientName"))
                    .phone(ctx.formParam("phone"))
                    .address(ctx.formParam("address"))
                    .subdistrict(ctx.formParam("subdistrict"))
                    .district(ctx.formParam("district"))
                    .city(ctx.formParam("city"))
                    .province(ctx.formParam("province"))
                    .postalCode(ctx.formParam("postalCode"))
                    .build());
            redirect(ctx, "/addresses", "success", "Alamat berhasil ditambahkan");
        } catch (Exception e) {
            redirect(ctx, "/addresses/add", "error", e.getMessage());
        }
    }

    public void setPrimaryAddress(Context ctx) {
        handleAddressAction(ctx, addressId -> {
            addressRepository.setPrimary(addressId, getUser(ctx).getId());
            redirect(ctx, "/addresses", "success", "Alamat utama berhasil diubah");
        }, "Gagal mengubah alamat utama");
    }

    public void deleteAddress(Context ctx) {
        handleAddressAction(ctx, addressId -> {
            addressRepository.delete(addressId, getUser(ctx).getId());
            redirect(ctx, "/addresses", "success", "Alamat berhasil dihapus");
        }, "Gagal menghapus alamat");
    }

    private void handleAddressAction(Context ctx, AddressAction action, String errorMsg) {
        User user = getUser(ctx);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        String addressId = ctx.formParam("addressId");
        if (addressId == null || addressId.isBlank()) {
            redirect(ctx, "/addresses", "error", "Address ID diperlukan");
            return;
        }

        try {
            action.execute(addressId);
        } catch (Exception e) {
            redirect(ctx, "/addresses", "error", errorMsg);
        }
    }

    @FunctionalInterface
    private interface AddressAction {

        void execute(String addressId) throws Exception;
    }

    private User getUser(Context ctx) {
        return ctx.sessionAttribute("currentUser");
    }

    private void redirect(Context ctx, String path, String param, String message) {
        ctx.redirect(path + "?" + param + "=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
