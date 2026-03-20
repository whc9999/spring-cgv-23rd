package com.ceos23.cgv.domain.concession.dto;

import com.ceos23.cgv.domain.concession.entity.Inventory;

public record InventoryResponse(
        Long inventoryId,
        String cinemaName,
        String productName,
        int stockQuantity
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getCinema().getName(),
                inventory.getProduct().getName(),
                inventory.getStockQuantity()
        );
    }
}