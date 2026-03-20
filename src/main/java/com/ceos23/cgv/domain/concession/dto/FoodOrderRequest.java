package com.ceos23.cgv.domain.concession.dto;

import java.util.List;

public record FoodOrderRequest(
        Long userId,
        Long cinemaId,
        List<OrderItemRequest> orderItems
) {
    public record OrderItemRequest(
            Long productId,
            int quantity
    ) {}
}