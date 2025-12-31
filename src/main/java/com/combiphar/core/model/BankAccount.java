package com.combiphar.core.model;

import java.util.Objects;

/**
 * Model untuk informasi rekening bank. Immutable class - thread-safe dan aman
 * untuk dibagikan.
 */
public final class BankAccount {

    private final String bankName;
    private final String accountNumber;
    private final String accountHolder;

    /**
     * Membuat instance BankAccount baru.
     *
     * @param bankName nama bank
     * @param accountNumber nomor rekening
     * @param accountHolder nama pemilik rekening
     * @throws IllegalArgumentException jika parameter tidak valid
     */
    public BankAccount(String bankName, String accountNumber, String accountHolder) {
        this.bankName = validateNotBlank(bankName, "Nama bank");
        this.accountNumber = validateNotBlank(accountNumber, "Nomor rekening");
        this.accountHolder = validateNotBlank(accountHolder, "Nama pemilik");
    }

    private String validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " tidak boleh kosong");
        }
        return value.trim();
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BankAccount that = (BankAccount) o;
        return accountNumber.equals(that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }
}
