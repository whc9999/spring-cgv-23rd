package com.ceos23.cgv.domain.reservation.dto;

import com.ceos23.cgv.domain.reservation.entity.ReservedSeat;

public record ReservedSeatResponse(
        Long reservedSeatId,
        String seatRow,
        int seatCol,
        Long screeningId
) {
    public static ReservedSeatResponse from(ReservedSeat reservedSeat) {
        return new ReservedSeatResponse(
                reservedSeat.getId(),
                reservedSeat.getSeatRow(),
                reservedSeat.getSeatCol(),
                reservedSeat.getScreening().getId()
        );
    }
}