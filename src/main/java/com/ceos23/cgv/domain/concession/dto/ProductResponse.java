package com.ceos23.cgv.domain.concession.dto;

import com.ceos23.cgv.domain.concession.entity.Product;
import com.ceos23.cgv.domain.concession.enums.ProductCategory;

public record ProductResponse(
        Long productId,
        String name,
        int price,
        String description,
        ProductCategory category
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory()
        );
    }
}