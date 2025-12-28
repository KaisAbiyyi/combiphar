package com.combiphar.core.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.combiphar.core.model.Item;
import com.combiphar.core.repository.ItemRepository;

/**
 * Service for Quality Control operations.
 */
public class QualityCheckService {

    private final ItemRepository itemRepository;

    public QualityCheckService() {
        this.itemRepository = new ItemRepository();
    }

    /**
     * Get today's QC pipeline (items that need quality check)
     */
    public List<Item> getTodayQCPipeline() {
        return itemRepository.findByEligibilityStatus("NEEDS_QC");
    }

    /**
     * Get items that need repair
     */
    public List<Item> getItemsNeedingRepair() {
        return itemRepository.findByEligibilityStatus("NEEDS_REPAIR");
    }

    /**
     * Get eligible items (passed QC)
     */
    public List<Item> getEligibleItems() {
        return itemRepository.findByEligibilityStatus("ELIGIBLE");
    }

    /**
     * Get QC statistics
     */
    public Map<String, Long> getQCStatistics() {
        List<Item> allItems = itemRepository.findAll();

        return allItems.stream()
                .collect(Collectors.groupingBy(
                        Item::getEligibilityStatus,
                        Collectors.counting()));
    }

    /**
     * Perform quality check on an item
     * 
     * @param itemId    The ID of the item to check
     * @param newStatus The new eligibility status (ELIGIBLE, NEEDS_QC,
     *                  NEEDS_REPAIR)
     * @param notes     Optional notes about the QC process
     * @return true if status was updated successfully
     */
    public boolean performQualityCheck(String itemId, String newStatus, String notes) {
        // Validate status
        if (!isValidEligibilityStatus(newStatus)) {
            throw new RuntimeException("Status tidak valid. Gunakan: ELIGIBLE, NEEDS_QC, atau NEEDS_REPAIR");
        }

        // Check if item exists
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan"));

        // If item is marked as ELIGIBLE, automatically publish it
        if (newStatus.equals("ELIGIBLE")) {
            item.setIsPublished(true);
            itemRepository.update(itemId, item);
        }

        // Update eligibility status
        return itemRepository.updateEligibilityStatus(itemId, newStatus);
    }

    /**
     * Batch quality check - approve multiple items at once
     */
    public int batchApproveItems(List<String> itemIds) {
        int approved = 0;

        for (String itemId : itemIds) {
            try {
                if (performQualityCheck(itemId, "ELIGIBLE", "Batch approved")) {
                    approved++;
                }
            } catch (Exception e) {
                // Log error but continue with other items
                System.err.println("Failed to approve item " + itemId + ": " + e.getMessage());
            }
        }

        return approved;
    }

    /**
     * Batch reject items - mark as NEEDS_REPAIR
     */
    public int batchRejectItems(List<String> itemIds) {
        int rejected = 0;

        for (String itemId : itemIds) {
            try {
                if (performQualityCheck(itemId, "NEEDS_REPAIR", "Batch rejected")) {
                    rejected++;
                }
            } catch (Exception e) {
                // Log error but continue with other items
                System.err.println("Failed to reject item " + itemId + ": " + e.getMessage());
            }
        }

        return rejected;
    }

    /**
     * Get daily QC summary
     */
    public QCSummary getDailyQCSummary() {
        Map<String, Long> stats = getQCStatistics();

        return new QCSummary(
                LocalDate.now(),
                stats.getOrDefault("NEEDS_QC", 0L).intValue(),
                stats.getOrDefault("ELIGIBLE", 0L).intValue(),
                stats.getOrDefault("NEEDS_REPAIR", 0L).intValue());
    }

    /**
     * Validate eligibility status
     */
    private boolean isValidEligibilityStatus(String status) {
        return status.equals("ELIGIBLE") ||
                status.equals("NEEDS_QC") ||
                status.equals("NEEDS_REPAIR");
    }

    /**
     * Inner class for QC Summary
     */
    public static class QCSummary {
        private LocalDate date;
        private int pendingQC;
        private int approved;
        private int needsRepair;

        public QCSummary(LocalDate date, int pendingQC, int approved, int needsRepair) {
            this.date = date;
            this.pendingQC = pendingQC;
            this.approved = approved;
            this.needsRepair = needsRepair;
        }

        // Getters
        public LocalDate getDate() {
            return date;
        }

        public int getPendingQC() {
            return pendingQC;
        }

        public int getApproved() {
            return approved;
        }

        public int getNeedsRepair() {
            return needsRepair;
        }

        public int getTotal() {
            return pendingQC + approved + needsRepair;
        }
    }
}
