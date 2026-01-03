package com.combiphar.core.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.combiphar.core.model.BankAccount;

/**
 * Service untuk logika pembayaran.
 */
public class PaymentService {

    private static final List<BankAccount> BANK_ACCOUNTS = List.of(
            new BankAccount("BCA", "192138123", "PT Combiphar Indonesia"),
            new BankAccount("Mandiri", "1234567890", "PT Combiphar Indonesia"),
            new BankAccount("BRI", "0987654321", "PT Combiphar Indonesia"));

    private final FileUploadService fileUploadService;

    public PaymentService(FileUploadService fileUploadService) {
        this.fileUploadService = Objects.requireNonNull(fileUploadService);
    }

    public List<BankAccount> getAvailableBankAccounts() {
        return Collections.unmodifiableList(BANK_ACCOUNTS);
    }

    public BankAccount getPrimaryBankAccount() {
        return BANK_ACCOUNTS.get(0);
    }

    public FileUploadService getFileUploadService() {
        return fileUploadService;
    }
}
