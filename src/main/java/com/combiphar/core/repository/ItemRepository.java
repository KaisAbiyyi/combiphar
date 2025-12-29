package com.combiphar.core.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.combiphar.core.config.DatabaseConfig;
import com.combiphar.core.model.Item;

/**
 * Repository for Item data access.
 */
public class ItemRepository {

    /**
     * Find all items
     */
    public List<Item> findAll() {
        String sql = "SELECT * FROM items ORDER BY created_at DESC";
        List<Item> items = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all items", e);
        }
        return items;
    }

    /**
     * Find item by ID
     */
    public Optional<Item> findById(String id) {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding item by id", e);
        }
        return Optional.empty();
    }

    /**
     * Find items by category ID
     */
    public List<Item> findByCategoryId(String categoryId) {
        String sql = "SELECT * FROM items WHERE category_id = ? ORDER BY created_at DESC";
        List<Item> items = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding items by category", e);
        }
        return items;
    }

    /**
     * Find items by eligibility status
     */
    public List<Item> findByEligibilityStatus(String status) {
        String sql = "SELECT * FROM items WHERE eligibility_status = ? ORDER BY created_at DESC";
        List<Item> items = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding items by eligibility status", e);
        }
        return items;
    }

    /**
     * Find published items
     */
    public List<Item> findPublished() {
        String sql = "SELECT * FROM items WHERE is_published = TRUE AND eligibility_status = 'ELIGIBLE' ORDER BY created_at DESC";
        List<Item> items = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding published items", e);
        }
        return items;
    }

    /**
     * Save new item
     */
    public Item save(Item item) {
        String sql = "INSERT INTO items (id, category_id, name, `condition`, description, image_url, price, stock, eligibility_status, is_published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (item.getId() == null) {
                item.setId(UUID.randomUUID().toString());
            }

            stmt.setString(1, item.getId());
            stmt.setString(2, item.getCategoryId());
            stmt.setString(3, item.getName());
            stmt.setString(4, item.getCondition());
            stmt.setString(5, item.getDescription());
            stmt.setString(6, item.getImageUrl());
            stmt.setBigDecimal(7, item.getPrice());
            stmt.setInt(8, item.getStock());
            stmt.setString(9, item.getEligibilityStatus() != null ? item.getEligibilityStatus() : "NEEDS_QC");
            stmt.setBoolean(10, item.getIsPublished() != null ? item.getIsPublished() : false);

            stmt.executeUpdate();

            return findById(item.getId()).orElse(item);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving item", e);
        }
    }

    /**
     * Update existing item
     */
    public Item update(String id, Item item) {
        String sql = "UPDATE items SET category_id = ?, name = ?, `condition` = ?, description = ?, image_url = ?, price = ?, stock = ?, eligibility_status = ?, is_published = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getCategoryId());
            stmt.setString(2, item.getName());
            stmt.setString(3, item.getCondition());
            stmt.setString(4, item.getDescription());
            stmt.setString(5, item.getImageUrl());
            stmt.setBigDecimal(6, item.getPrice());
            stmt.setInt(7, item.getStock());
            stmt.setString(8, item.getEligibilityStatus());
            stmt.setBoolean(9, item.getIsPublished());
            stmt.setString(10, id);

            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RuntimeException("Item not found with id: " + id);
            }

            return findById(id).orElse(item);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating item", e);
        }
    }

    /**
     * Update eligibility status only
     */
    public boolean updateEligibilityStatus(String id, String status) {
        String sql = "UPDATE items SET eligibility_status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, id);

            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating item eligibility status", e);
        }
    }

    /**
     * Update stock quantity
     */
    public boolean updateStock(String id, int quantity) {
        String sql = "UPDATE items SET stock = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setString(2, id);

            int updated = stmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating item stock", e);
        }
    }

    /**
     * Delete item by ID
     */
    public boolean deleteById(String id) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int deleted = stmt.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting item", e);
        }
    }

    /**
     * Delete all items by category ID
     */
    public int deleteByCategoryId(String categoryId) {
        String sql = "DELETE FROM items WHERE category_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting items by category", e);
        }
    }

    /**
     * Count items by category ID
     */
    public int countByCategoryId(String categoryId) {
        String sql = "SELECT COUNT(*) FROM items WHERE category_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting items by category", e);
        }
        return 0;
    }

    /**
     * Search published items with filters for customer catalog
     * 
     * @param searchQuery search term for name (can be null)
     * @param categoryId  filter by category (can be null)
     * @return list of matching published items
     */
    public List<Item> searchPublishedItems(String searchQuery, String categoryId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM items WHERE is_published = TRUE AND eligibility_status = 'ELIGIBLE' AND stock > 0");

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ?)");
        }
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            sql.append(" AND category_id = ?");
        }
        sql.append(" ORDER BY created_at DESC");

        List<Item> items = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                String searchPattern = "%" + searchQuery.trim() + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            if (categoryId != null && !categoryId.trim().isEmpty()) {
                stmt.setString(paramIndex++, categoryId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching published items", e);
        }
        return items;
    }

    /**
     * Map ResultSet to Item object
     */
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getString("id"));
        item.setCategoryId(rs.getString("category_id"));
        item.setName(rs.getString("name"));
        item.setCondition(rs.getString("condition"));
        item.setDescription(rs.getString("description"));
        item.setImageUrl(rs.getString("image_url"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setStock(rs.getInt("stock"));
        item.setEligibilityStatus(rs.getString("eligibility_status"));
        item.setIsPublished(rs.getBoolean("is_published"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return item;
    }
}
