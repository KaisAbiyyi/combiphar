package com.combiphar.core.model;

/**
 * Represents a shipping address for order delivery. Value object with
 * validation.
 */
public class ShippingAddress {

    private final String recipientName;
    private final String address;
    private final String city;
    private final String postalCode;
    private final String phone;

    /**
     * Creates a shipping address with validation.
     *
     * @throws IllegalArgumentException if any required field is invalid
     */
    public ShippingAddress(String recipientName, String address, String city,
            String postalCode) {
        this(recipientName, address, city, postalCode, null);
    }

    /**
     * Creates a shipping address with optional phone number.
     *
     * @throws IllegalArgumentException if any required field is invalid
     */
    public ShippingAddress(String recipientName, String address, String city,
            String postalCode, String phone) {
        validateField(recipientName, "Nama penerima");
        validateField(address, "Alamat");
        validateField(city, "Kota");
        validateField(postalCode, "Kode pos");

        this.recipientName = recipientName;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.phone = phone != null ? phone.trim() : "";
    }

    private void validateField(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " wajib diisi");
        }
    }

    public String getRecipientName() {
        return recipientName;
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

    public String getPhone() {
        return phone;
    }

    /**
     * Returns formatted address for display.
     */
    public String getFormattedAddress() {
        return String.format("%s, %s, %s", address, city, postalCode);
    }
}
