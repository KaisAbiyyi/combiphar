package com.combiphar.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a saved user address. Immutable value object with validation.
 */
public class Address {

    private final String id;
    private final String userId;
    private final String recipientName;
    private final String phone;
    private final String address;
    private final String subdistrict;
    private final String district;
    private final String city;
    private final String province;
    private final String postalCode;
    private final boolean primary;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Address(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID().toString();
        this.userId = builder.userId;
        this.recipientName = safe(builder.recipientName);
        this.phone = safe(builder.phone);
        this.address = safe(builder.address);
        this.subdistrict = safe(builder.subdistrict);
        this.district = safe(builder.district);
        this.city = safe(builder.city);
        this.province = safe(builder.province);
        this.postalCode = safe(builder.postalCode);
        this.primary = builder.primary;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    /**
     * Validates required fields before saving.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validateForSave() {
        require(recipientName, "Nama penerima");
        require(phone, "Nomor telepon");
        require(address, "Alamat");
        require(city, "Kota");
        require(postalCode, "Kode pos");
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " wajib diisi");
        }
    }

    /**
     * Returns formatted full address for display.
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(address);
        if (!subdistrict.isEmpty()) {
            sb.append(", ").append(subdistrict);
        }
        if (!district.isEmpty()) {
            sb.append(", ").append(district);
        }
        sb.append(", ").append(city);
        if (!province.isEmpty()) {
            sb.append(", ").append(province);
        }
        sb.append(" ").append(postalCode);
        return sb.toString();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getSubdistrict() {
        return subdistrict;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public boolean isPrimary() {
        return primary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Builder for creating Address instances.
     */
    public static class Builder {

        private String id;
        private String userId;
        private String recipientName;
        private String phone;
        private String address;
        private String subdistrict;
        private String district;
        private String city;
        private String province;
        private String postalCode;
        private boolean primary;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder recipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder subdistrict(String subdistrict) {
            this.subdistrict = subdistrict;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder province(String province) {
            this.province = province;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder primary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Address build() {
            return new Address(this);
        }
    }
}
