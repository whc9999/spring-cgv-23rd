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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservedSeatService {

    private final ReservedSeatRepository reservedSeatRepository;
    private final ReservationRepository reservationRepository;
    private final ScreeningRepository screeningRepository;

    @Transactional
    public List<ReservedSeat> createReservedSeats(ReservedSeatRequest request) {
        Reservation reservation = findReservation(request.reservationId());
        Screening screening = findScreening(request.screeningId());
        List<ReservedSeat> reservedSeats = createReservedSeats(request, reservation, screening);

        try {
            return reservedSeatRepository.saveAll(reservedSeats);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private Screening findScreening(Long screeningId) {
        return screeningRepository.findByIdForUpdate(screeningId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCREENING_NOT_FOUND));
    }

    private List<ReservedSeat> createReservedSeats(ReservedSeatRequest request,
                                                   Reservation reservation,
                                                   Screening screening) {
        return request.seats().stream()
                .map(seatInfo -> ReservedSeat.create(
                        reservation,
                        screening,
                        seatInfo.row(),
                        seatInfo.col()
                ))
                .toList();
    }

    public List<ReservedSeat> getReservedSeatsByScreeningId(Long screeningId) {
        return reservedSeatRepository.findByScreeningId(screeningId);
    }
}
