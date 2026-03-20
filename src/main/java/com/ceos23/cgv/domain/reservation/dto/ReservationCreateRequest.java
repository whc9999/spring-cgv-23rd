package com.ceos23.cgv.domain.reservation.dto;

import com.ceos23.cgv.domain.reservation.enums.Payment;

public record ReservationCreateRequest(
        Long userId,
        Long screeningId,
        int peopleCount,
        Payment payment,
        String couponCode
) {
}