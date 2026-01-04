package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Finds all users with optional filtering.
     */
    public List<User> findAll(String roleFilter, String statusFilter, String searchQuery) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (roleFilter != null && !roleFilter.isEmpty() && !"all".equals(roleFilter)) {
            sql.append(" AND role = ?");
            params.add(roleFilter);
        }

        if (statusFilter != null && !statusFilter.isEmpty() && !"all".equals(statusFilter)) {
            sql.append(" AND status = ?");
            params.add(statusFilter);
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            sql.append(" AND (name LIKE ? OR email LIKE ?)");
            params.add("%" + searchQuery + "%");
            params.add("%" + searchQuery + "%");
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            List<User> users = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
    }

    /**
     * Counts total users.
     */
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting total users", e);
        }
        return 0;
    }

    /**
     * Counts users by role.
     */
    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting users by role", e);
        }
        return 0;
    }

    /**
     * Counts active users.
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting users by status", e);
        }
        return 0;
    }

    /**
     * Updates user status.
     */
    public void updateStatus(String userId, String status) {
        String sql = "UPDATE users SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user status", e);
        }
    }

    /**
     * Updates user role.
     */
    public void updateRole(String userId, Role role) {
        String sql = "UPDATE users SET role = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user role", e);
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
