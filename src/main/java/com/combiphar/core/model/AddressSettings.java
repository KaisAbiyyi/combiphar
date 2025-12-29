package com.combiphar.core.model;

/**
 * Represents user address settings for shipping preferences. Mutable-free value
 * object with explicit validation.
 */
public class AddressSettings {

    private final String recipientName;
    private final String phone;
    private final String companyName;
    private final String address;
    private final String city;
    private final String postalCode;
    private final String shippingMethod;
    private final String courierNotes;
    private final boolean termsAccepted;

    public AddressSettings(String recipientName, String phone, String companyName,
            String address, String city, String postalCode,
            String shippingMethod, String courierNotes, boolean termsAccepted) {
        this.recipientName = safe(recipientName);
        this.phone = safe(phone);
        this.companyName = safe(companyName);
        this.address = safe(address);
        this.city = safe(city);
        this.postalCode = safe(postalCode);
        this.shippingMethod = safe(shippingMethod);
        this.courierNotes = safe(courierNotes);
        this.termsAccepted = termsAccepted;
    }

    public void validateForSave() {
        require(recipientName, "Nama penerima");
        require(phone, "Nomor telepon");
        require(address, "Alamat");
        require(city, "Kota");
        require(postalCode, "Kode pos");
        require(shippingMethod, "Metode pengiriman");
        if (!termsAccepted) {
            throw new IllegalArgumentException("Anda harus menyetujui ketentuan pembelian");
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " wajib diisi");
        }
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public String getCourierNotes() {
        return courierNotes;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }
}
