package com.ceos23.cgv.domain.reservation.dto;

import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.enums.Payment;
import com.ceos23.cgv.domain.reservation.enums.ReservationStatus;

public record ReservationResponse(
        Long reservationId,
        String userName,
        String movieTitle,
        String cinemaName,
        String theaterName,
        ReservationStatus status,
        int peopleCount,
        int price,
        Payment payment,
        String saleNumber
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getName(),
                reservation.getScreening().getMovie().getTitle(),
                reservation.getScreening().getTheater().getCinema().getName(),
                reservation.getScreening().getTheater().getName(),
                reservation.getStatus(),
                reservation.getPeopleCount(),
                reservation.getPrice(),
                reservation.getPayment(),
                reservation.getSaleNumber()
        );
    }
}