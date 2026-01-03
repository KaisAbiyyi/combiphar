package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Role;
import com.combiphar.core.model.User;

/**
 * Repository for User data access.
 */
public class UserRepository {

    /**
     * Finds a user by ID.
     */
    public Optional<User> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by email.
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    /**
     * Saves a new user to the database.
     */
    public void save(User user) {
        String sql = "INSERT INTO users (id, name, email, password, role, phone, address, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (user.getId() == null) {
                user.setId(UUID.randomUUID().toString());
            }

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getAddress());
            stmt.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setStatus(rs.getString("status"));
        return user;
    }
}
