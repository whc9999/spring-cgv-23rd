package com.ceos23.cgv.domain.reservation.dto;

import com.ceos23.cgv.domain.reservation.enums.Payment;

import java.util.List;

public record ReservationCreateRequest(
        Long screeningId,
        int peopleCount,
        Payment payment,
        String couponCode,
        List<ReservedSeatRequest.SeatInfo> seats
) {
}
