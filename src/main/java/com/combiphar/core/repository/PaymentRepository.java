package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Payment;

/**
 * Repository untuk Payment data access.
 */
public class PaymentRepository {

    /**
     * Menyimpan payment baru ke database.
     */
    public void save(Payment payment) {
        String sql = "INSERT INTO payments (id, order_id, type, bank, amount, status, proof, paid_at, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, payment.getId());
            stmt.setString(2, payment.getOrderId());
            stmt.setString(3, payment.getType());
            stmt.setString(4, payment.getBank());
            stmt.setBigDecimal(5, payment.getAmount());
            stmt.setString(6, payment.getStatus());
            stmt.setString(7, payment.getProofFilePath());

            if (payment.getPaidAt() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(payment.getPaidAt()));
            } else {
                stmt.setTimestamp(8, null);
            }

            stmt.setTimestamp(9, Timestamp.valueOf(payment.getCreatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving payment: " + e.getMessage(), e);
        }
    }

    /**
     * Update payment dengan bukti pembayaran dan bank.
     */
    public void updatePayment(String paymentId, String proofFilePath, String bank) {
        String sql = "UPDATE payments SET status = 'SUCCESS', proof = ?, bank = ?, paid_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, proofFilePath);
            stmt.setString(2, bank);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, paymentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating payment: " + e.getMessage(), e);
        }
    }

    /**
     * Update status payment (SUCCESS/FAILED).
     */
    public void updateStatus(String orderId, String status) {
        if (status == null || (!status.equals("SUCCESS") && !status.equals("FAILED"))) {
            throw new IllegalArgumentException("Status must be SUCCESS or FAILED");
        }

        String sql = "UPDATE payments SET status = ?, paid_at = ? WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setTimestamp(2, status.equals("SUCCESS") ? new Timestamp(System.currentTimeMillis()) : null);
            stmt.setString(3, orderId);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Payment not found for order: " + orderId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating payment status: " + e.getMessage(), e);
        }
    }

    /**
     * Mencari payment berdasarkan order ID.
     */
    public Optional<Payment> findByOrderId(String orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payment: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Timestamp paidAtTs = rs.getTimestamp("paid_at");
        return new Payment(
                rs.getString("id"),
                rs.getString("order_id"),
                rs.getString("type"),
                rs.getString("bank"),
                rs.getBigDecimal("amount"),
                rs.getString("status"),
                rs.getString("proof"),
                paidAtTs != null ? paidAtTs.toLocalDateTime() : null,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
