package com.combiphar.core.model;

/**
 * Enum untuk status pembayaran. Single Responsibility: hanya mendefinisikan
 * status pembayaran.
 */
public enum PaymentStatus {

    PENDING("Menunggu Pembayaran"),
    UPLOADED("Bukti Diunggah"),
    VERIFIED("Terverifikasi"),
    REJECTED("Ditolak");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
