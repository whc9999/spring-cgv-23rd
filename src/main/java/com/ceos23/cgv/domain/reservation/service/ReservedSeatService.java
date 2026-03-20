package com.ceos23.cgv.domain.reservation.service;

import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import com.ceos23.cgv.domain.reservation.dto.ReservedSeatRequest;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.entity.ReservedSeat;
import com.ceos23.cgv.domain.reservation.repository.ReservationRepository;
import com.ceos23.cgv.domain.reservation.repository.ReservedSeatRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservedSeatService {

    private final ReservedSeatRepository reservedSeatRepository;
    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;

    @Transactional
    public List<ReservedSeat> createReservedSeats(ReservedSeatRequest request) {

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCREENING_NOT_FOUND));

        List<ReservedSeat> reservedSeats = request.seats().stream()
                .map(seatInfo -> ReservedSeat.builder()
                        .reservation(reservation)
                        .screening(screening)
                        .seatRow(seatInfo.row())
                        .seatCol(seatInfo.col())
                        .build())
                .collect(Collectors.toList());

        try {
            return reservedSeatRepository.saveAll(reservedSeats);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
    }

    public List<ReservedSeat> getReservedSeatsByScreeningId(Long screeningId) {
        return reservedSeatRepository.findByScreeningId(screeningId);
    }
}