package com.ceos23.cgv.domain.concession.dto;

public record InventoryUpdateRequest(
        Long cinemaId,
        Long productId,
        int quantity
) {
}