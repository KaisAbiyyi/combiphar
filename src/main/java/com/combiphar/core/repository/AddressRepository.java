package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Address;

/**
 * Repository for Address persistence. Follows defensive programming and SOLID.
 */
public class AddressRepository {

    /**
     * Finds all addresses for a user, ordered by primary first, then by
     * creation date.
     */
    public List<Address> findByUserId(String userId) throws SQLException {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }

        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_primary DESC, created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addresses.add(mapRow(rs));
                }
            }
        }
        return addresses;
    }

    /**
     * Finds a single address by ID.
     */
    public Optional<Address> findById(String id) throws SQLException {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM addresses WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the primary address for a user.
     */
    public Optional<Address> findPrimaryByUserId(String userId) throws SQLException {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM addresses WHERE user_id = ? AND is_primary = TRUE LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Saves a new address. If this is the first address for the user, sets it
     * as primary.
     */
    public void save(Address address) throws SQLException {
        if (address == null) {
            throw new IllegalArgumentException("Address tidak boleh null");
        }
        address.validateForSave();

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if this should be primary (first address OR explicitly requested)
                String checkSql = "SELECT COUNT(*) FROM addresses WHERE user_id = ?";
                boolean isFirstAddress;
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, address.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        isFirstAddress = rs.next() && rs.getInt(1) == 0;
                    }
                }

                boolean isPrimary = isFirstAddress || address.isPrimary();
                if (isPrimary) {
                    clearPrimary(conn, address.getUserId());
                }

                String sql = "INSERT INTO addresses (id, user_id, recipient_name, phone, address, "
                        + "subdistrict, district, city, province, postal_code, is_primary, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, address.getId());
                    ps.setString(2, address.getUserId());
                    ps.setString(3, address.getRecipientName());
                    ps.setString(4, address.getPhone());
                    ps.setString(5, address.getAddress());
                    ps.setString(6, address.getSubdistrict());
                    ps.setString(7, address.getDistrict());
                    ps.setString(8, address.getCity());
                    ps.setString(9, address.getProvince());
                    ps.setString(10, address.getPostalCode());
                    ps.setBoolean(11, isPrimary);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Sets an address as primary for the user.
     */
    public void setPrimary(String addressId, String userId) throws SQLException {
        if (addressId == null || userId == null) {
            throw new IllegalArgumentException("Address ID dan User ID diperlukan");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                clearPrimary(conn, userId);
                String sql = "UPDATE addresses SET is_primary = TRUE, updated_at = NOW() WHERE id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, addressId);
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Deletes an address by ID.
     */
    public void delete(String addressId, String userId) throws SQLException {
        if (addressId == null || userId == null) {
            throw new IllegalArgumentException("Address ID dan User ID diperlukan");
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String checkSql = "SELECT is_primary FROM addresses WHERE id = ? AND user_id = ?";
                boolean wasPrimary;
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, addressId);
                    ps.setString(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        wasPrimary = rs.next() && rs.getBoolean("is_primary");
                    }
                }

                String deleteSql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, addressId);
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }

                if (wasPrimary) {
                    String promoteSql = "UPDATE addresses SET is_primary = TRUE, updated_at = NOW() "
                            + "WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
                    try (PreparedStatement ps = conn.prepareStatement(promoteSql)) {
                        ps.setString(1, userId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void clearPrimary(Connection conn, String userId) throws SQLException {
        String sql = "UPDATE addresses SET is_primary = FALSE WHERE user_id = ? AND is_primary = TRUE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        }
    }

    private Address mapRow(ResultSet rs) throws SQLException {
        return new Address.Builder()
                .id(rs.getString("id"))
                .userId(rs.getString("user_id"))
                .recipientName(rs.getString("recipient_name"))
                .phone(rs.getString("phone"))
                .address(rs.getString("address"))
                .subdistrict(rs.getString("subdistrict"))
                .district(rs.getString("district"))
                .city(rs.getString("city"))
                .province(rs.getString("province"))
                .postalCode(rs.getString("postal_code"))
                .primary(rs.getBoolean("is_primary"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }
}
