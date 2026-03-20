package com.ceos23.cgv.domain.concession.dto;

import com.ceos23.cgv.domain.concession.enums.ProductCategory;

public record ProductCreateRequest(
        String name,
        int price,
        String description,
        String origin,
        String ingredient,
        Boolean pickupPossible,
        ProductCategory category
) {
}