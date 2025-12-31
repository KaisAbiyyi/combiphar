package com.combiphar.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.combiphar.core.model.PaymentProof;

/**
 * Service untuk menangani upload file bukti pembayaran. Single Responsibility:
 * hanya menangani operasi file.
 */
public class FileUploadService {

    private static final String UPLOAD_DIR = "uploads/payment-proofs";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Menyimpan file yang diunggah dan mengembalikan PaymentProof.
     *
     * @param inputStream stream file yang diunggah
     * @param originalFileName nama file asli
     * @param contentType tipe MIME file
     * @param fileSize ukuran file
     * @return PaymentProof yang berhasil dibuat
     * @throws IOException jika gagal menyimpan file
     * @throws IllegalArgumentException jika file tidak valid
     */
    public PaymentProof saveFile(InputStream inputStream, String originalFileName,
            String contentType, long fileSize) throws IOException {
        validateFile(originalFileName, contentType, fileSize);

        String savedFileName = generateUniqueFileName(originalFileName);
        Path uploadPath = getUploadPath();
        Path filePath = uploadPath.resolve(savedFileName);

        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        return new PaymentProof(originalFileName, filePath.toString(), contentType, fileSize);
    }

    private void validateFile(String fileName, String contentType, long fileSize) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Nama file tidak boleh kosong");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ukuran file melebihi batas 5MB");
        }
        if (!isAllowedType(contentType)) {
            throw new IllegalArgumentException("Tipe file tidak diizinkan");
        }
    }

    private boolean isAllowedType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String type = contentType.toLowerCase();
        return type.startsWith("image/") || type.equals("application/pdf");
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + extension;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }

    private Path getUploadPath() throws IOException {
        Path path = Paths.get(UPLOAD_DIR);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }
}
