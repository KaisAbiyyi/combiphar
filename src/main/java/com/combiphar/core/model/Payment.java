package com.combiphar.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Model untuk Payment.
 */
public class Payment {

    private final String id;
    private final String orderId;
    private final String type;
    private final String bank;
    private final BigDecimal amount;
    private String status;
    private String proofFilePath;
    private LocalDateTime paidAt;
    private final LocalDateTime createdAt;

    public Payment(String orderId, BigDecimal amount, String bank) {
        this.id = UUID.randomUUID().toString();
        this.orderId = Objects.requireNonNull(orderId);
        this.type = "TRANSFER";
        this.bank = bank;
        this.amount = Objects.requireNonNull(amount);
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Payment(String id, String orderId, String type, String bank, BigDecimal amount,
            String status, String proofFilePath, LocalDateTime paidAt,
            LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.type = type;
        this.bank = bank;
        this.amount = amount;
        this.status = status;
        this.proofFilePath = proofFilePath;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public void markAsPaid(String proofFilePath) {
        this.status = "SUCCESS";
        this.proofFilePath = proofFilePath;
        this.paidAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getType() {
        return type;
    }

    public String getBank() {
        return bank;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getProofFilePath() {
        return proofFilePath;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
