package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Shipment;
import com.combiphar.core.model.Shipment.Status;

/**
 * Repository untuk Shipment data access. Single Responsibility: hanya menangani
 * database operations untuk Shipment.
 */
public class ShipmentRepository {

    /**
     * Menyimpan shipment baru ke database.
     *
     * @param shipment shipment yang akan disimpan
     */
    public void save(Shipment shipment) {
        String sql = "INSERT INTO shipments (id, order_id, address_id, courier_name, tracking_number, "
                + "shipment_status, shipped_at, delivered_at, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shipment.getId());
            stmt.setString(2, shipment.getOrderId());
            stmt.setString(3, shipment.getAddressId());
            stmt.setString(4, shipment.getCourierName());
            stmt.setString(5, shipment.getTrackingNumber());
            stmt.setString(6, shipment.getStatus().name());
            stmt.setTimestamp(7, toTimestamp(shipment.getShippedAt()));
            stmt.setTimestamp(8, toTimestamp(shipment.getDeliveredAt()));
            stmt.setTimestamp(9, Timestamp.valueOf(shipment.getCreatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving shipment: " + e.getMessage(), e);
        }
    }

    /**
     * Mencari shipment berdasarkan ID.
     *
     * @param id ID shipment
     * @return Optional berisi shipment jika ditemukan
     */
    public Optional<Shipment> findById(String id) {
        String sql = "SELECT * FROM shipments WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding shipment: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Mencari shipment berdasarkan order ID.
     *
     * @param orderId ID order
     * @return Optional berisi shipment jika ditemukan
     */
    public Optional<Shipment> findByOrderId(String orderId) {
        String sql = "SELECT * FROM shipments WHERE order_id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding shipment by order: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Mencari semua shipment, diurutkan dari yang terbaru.
     *
     * @return list semua shipment
     */
    public List<Shipment> findAll() {
        List<Shipment> shipments = new ArrayList<>();
        String sql = "SELECT * FROM shipments ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                shipments.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all shipments: " + e.getMessage(), e);
        }
        return shipments;
    }

    /**
     * Update nomor resi dan status menjadi SHIPPED.
     *
     * @param shipmentId ID shipment
     * @param trackingNumber nomor resi
     */
    public void updateTrackingNumber(String shipmentId, String trackingNumber) {
        String sql = "UPDATE shipments SET tracking_number = ?, shipment_status = 'SHIPPED', "
                + "shipped_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trackingNumber);
            stmt.setString(2, shipmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating tracking number: " + e.getMessage(), e);
        }
    }

    /**
     * Update status shipment.
     *
     * @param shipmentId ID shipment
     * @param status status baru
     */
    public void updateStatus(String shipmentId, Status status) {
        String sql = "UPDATE shipments SET shipment_status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setString(2, shipmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating shipment status: " + e.getMessage(), e);
        }
    }

    /**
     * Tandai shipment sebagai delivered.
     *
     * @param shipmentId ID shipment
     */
    public void markAsDelivered(String shipmentId) {
        String sql = "UPDATE shipments SET shipment_status = 'DELIVERED', "
                + "delivered_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shipmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking as delivered: " + e.getMessage(), e);
        }
    }

    /**
     * Tandai shipment sebagai received (diterima user).
     *
     * @param shipmentId ID shipment
     */
    public void markAsReceived(String shipmentId) {
        String sql = "UPDATE shipments SET shipment_status = 'RECEIVED' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shipmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking as received: " + e.getMessage(), e);
        }
    }

    // === Private Helper Methods ===
    private Shipment mapResultSet(ResultSet rs) throws SQLException {
        return new Shipment(
                rs.getString("id"),
                rs.getString("order_id"),
                rs.getString("address_id"),
                rs.getString("courier_name"),
                rs.getString("tracking_number"),
                parseStatus(rs.getString("shipment_status")),
                toLocalDateTime(rs.getTimestamp("shipped_at")),
                toLocalDateTime(rs.getTimestamp("delivered_at")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    private Status parseStatus(String status) {
        if (status == null) {
            return Status.PENDING;
        }
        try {
            return Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            return Status.PENDING;
        }
    }

    private Timestamp toTimestamp(java.time.LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
