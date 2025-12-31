package com.combiphar.core.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.combiphar.core.model.BankAccount;
import com.combiphar.core.model.PaymentMethod;

/**
 * Service untuk menangani logika pembayaran. Single Responsibility: hanya
 * menangani operasi terkait pembayaran.
 */
public class PaymentService {

    // Rekening bank yang tersedia untuk transfer
    private static final List<BankAccount> BANK_ACCOUNTS = List.of(
            new BankAccount("BCA", "192138123", "PT Combiphar Indonesia"),
            new BankAccount("Mandiri", "1234567890", "PT Combiphar Indonesia"),
            new BankAccount("BRI", "0987654321", "PT Combiphar Indonesia")
    );

    private final FileUploadService fileUploadService;

    public PaymentService(FileUploadService fileUploadService) {
        this.fileUploadService = Objects.requireNonNull(fileUploadService);
    }

    /**
     * Mendapatkan daftar rekening bank yang tersedia.
     *
     * @return list rekening bank (unmodifiable)
     */
    public List<BankAccount> getAvailableBankAccounts() {
        return Collections.unmodifiableList(BANK_ACCOUNTS);
    }

    /**
     * Mendapatkan rekening bank utama untuk transfer.
     *
     * @return BankAccount utama
     */
    public BankAccount getPrimaryBankAccount() {
        return BANK_ACCOUNTS.get(0);
    }

    /**
     * Mendapatkan metode pembayaran yang tersedia.
     *
     * @return array PaymentMethod
     */
    public PaymentMethod[] getAvailablePaymentMethods() {
        return PaymentMethod.values();
    }

    /**
     * Memvalidasi jumlah pembayaran.
     *
     * @param amount jumlah yang akan dibayar
     * @throws IllegalArgumentException jika jumlah tidak valid
     */
    public void validatePaymentAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Jumlah pembayaran harus lebih dari 0");
        }
    }

    /**
     * Mendapatkan FileUploadService.
     *
     * @return FileUploadService instance
     */
    public FileUploadService getFileUploadService() {
        return fileUploadService;
    }
}
