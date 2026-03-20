package com.ceos23.cgv.domain.reservation.dto;

import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.enums.Payment;
import com.ceos23.cgv.domain.reservation.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationResponse {
    private Long reservationId;
    private String userName;
    private String movieTitle;
    private String cinemaName;
    private String theaterName;
    private ReservationStatus status;
    private int peopleCount;
    private int price;
    private Payment payment;
    private String saleNumber;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .userName(reservation.getUser().getName())
                .movieTitle(reservation.getScreening().getMovie().getTitle())
                .cinemaName(reservation.getScreening().getTheater().getCinema().getName())
                .theaterName(reservation.getScreening().getTheater().getName())
                .status(reservation.getStatus())
                .peopleCount(reservation.getPeopleCount())
                .price(reservation.getPrice())
                .payment(reservation.getPayment())
                .saleNumber(reservation.getSaleNumber())
                .build();
    }
}