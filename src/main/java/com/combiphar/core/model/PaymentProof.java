package com.combiphar.core.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model untuk bukti pembayaran yang diunggah. Immutable class - data tidak bisa
 * diubah setelah dibuat.
 */
public final class PaymentProof {

    private final String fileName;
    private final String filePath;
    private final String contentType;
    private final long fileSize;
    private final LocalDateTime uploadedAt;

    /**
     * Membuat instance PaymentProof baru.
     *
     * @param fileName nama file asli
     * @param filePath path penyimpanan file
     * @param contentType tipe MIME file
     * @param fileSize ukuran file dalam bytes
     */
    public PaymentProof(String fileName, String filePath, String contentType, long fileSize) {
        this.fileName = validateNotBlank(fileName, "Nama file");
        this.filePath = validateNotBlank(filePath, "Path file");
        this.contentType = validateContentType(contentType);
        this.fileSize = validateFileSize(fileSize);
        this.uploadedAt = LocalDateTime.now();
    }

    private String validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " tidak boleh kosong");
        }
        return value.trim();
    }

    private String validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Tipe file tidak valid");
        }
        String type = contentType.toLowerCase();
        if (!isAllowedContentType(type)) {
            throw new IllegalArgumentException("Tipe file tidak diizinkan: " + contentType);
        }
        return type;
    }

    private boolean isAllowedContentType(String type) {
        return type.startsWith("image/") || type.equals("application/pdf");
    }

    private long validateFileSize(long size) {
        // Max 5MB
        long maxSize = 5 * 1024 * 1024;
        if (size <= 0) {
            throw new IllegalArgumentException("Ukuran file tidak valid");
        }
        if (size > maxSize) {
            throw new IllegalArgumentException("Ukuran file melebihi batas 5MB");
        }
        return size;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentProof that = (PaymentProof) o;
        return filePath.equals(that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }
}
