package com.combiphar.core.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Category;

/**
 * Repository for Category data access.
 */
public class CategoryRepository {

    /**
     * Find all categories
     */
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY created_at DESC";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all categories", e);
        }
        return categories;
    }

    /**
     * Find categories by status
     */
    public List<Category> findByStatus(String status) {
        String sql = "SELECT * FROM categories WHERE status = ? ORDER BY created_at DESC";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding categories by status", e);
        }
        return categories;
    }

    /**
     * Find category by ID
     */
    public Optional<Category> findById(String id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding category by id", e);
        }
        return Optional.empty();
    }

    /**
     * Find category by name
     */
    public Optional<Category> findByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding category by name", e);
        }
        return Optional.empty();
    }

    /**
     * Check if category exists by name
     */
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking category existence", e);
        }
        return false;
    }

    /**
     * Save new category
     */
    public Category save(Category category) {
        String sql = "INSERT INTO categories (id, name, description, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (category.getId() == null) {
                category.setId(UUID.randomUUID().toString());
            }
            if (category.getStatus() == null) {
                category.setStatus("AKTIF");
            }

            stmt.setString(1, category.getId());
            stmt.setString(2, category.getName());
            stmt.setString(3, category.getDescription());
            stmt.setString(4, category.getStatus());

            stmt.executeUpdate();

            // Fetch created category to get timestamp
            return findById(category.getId()).orElse(category);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving category", e);
        }
    }

    /**
     * Update existing category
     */
    public Category update(String id, Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getStatus());
            stmt.setString(4, id);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Category not found with id: " + id);
            }

            return findById(id).orElse(category);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
    }

    /**
     * Delete category by ID
     */
    public boolean deleteById(String id) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int deleted = stmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    /**
     * Map ResultSet to Category object
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getString("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            category.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return category;
    }
}
