package com.combiphar.core.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents an item/product in the inventory system.
 */
public class Item {
    private String id;
    private String categoryId;
    private String name;
    private String condition; // NEW, USED_GOOD, USED_FAIR
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
    private String eligibilityStatus; // ELIGIBLE, NEEDS_QC, NEEDS_REPAIR
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Item() {
    }

    public Item(String id, String categoryId, String name, String condition, BigDecimal price, Integer stock) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.condition = condition;
        this.price = price;
        this.stock = stock;
        this.eligibilityStatus = "NEEDS_QC"; // Default status
        this.isPublished = false; // Default not published
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", condition='" + condition + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", eligibilityStatus='" + eligibilityStatus + '\'' +
                ", isPublished=" + isPublished +
                '}';
    }
}
