package com.combiphar.core.util;

import java.util.List;

/**
 * Simple pagination utility. Immutable and defensive.
 */
public class Pagination<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final int totalItems;
    private final int totalPages;

    public Pagination(List<T> allItems, int page, int pageSize) {
        if (allItems == null) {
            throw new IllegalArgumentException("Items cannot be null");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        
        this.totalItems = allItems.size();
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
        this.currentPage = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, totalItems);
        this.items = allItems.subList(start, end);
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasNext() {
        return currentPage < totalPages;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }
}
