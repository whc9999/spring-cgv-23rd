package com.ceos23.cgv.domain.reservation.dto;

import java.util.List;

public record ReservedSeatRequest(
        Long reservationId,
        Long screeningId,
        List<SeatInfo> seats
) {
    public record SeatInfo(
            String row,
            int col
    ) {}
}