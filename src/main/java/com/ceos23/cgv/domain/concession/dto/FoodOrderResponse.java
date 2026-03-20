package com.ceos23.cgv.domain.concession.dto;

import com.ceos23.cgv.domain.concession.entity.FoodOrder;

public record FoodOrderResponse(
        Long orderId,
        String userName,
        String cinemaName,
        int totalPrice
) {
    public static FoodOrderResponse from(FoodOrder foodOrder) {
        return new FoodOrderResponse(
                foodOrder.getId(),
                foodOrder.getUser().getNickname(),
                foodOrder.getCinema().getName(),
                foodOrder.getTotalPrice()
        );
    }
}