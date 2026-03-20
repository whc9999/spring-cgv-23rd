package com.ceos23.cgv.domain.reservation.service;

import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import com.ceos23.cgv.domain.reservation.dto.ReservedSeatRequest;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.entity.ReservedSeat;
import com.ceos23.cgv.domain.reservation.repository.ReservationRepository;
import com.ceos23.cgv.domain.reservation.repository.ReservedSeatRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("예매 정보를 찾을 수 없습니다."));

        Screening screening = screeningRepository.findById(request.screeningId())
                .orElseThrow(() -> new IllegalArgumentException("상영 일정을 찾을 수 없습니다."));

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
            throw new IllegalStateException("이미 예매가 완료된 좌석이 포함되어 있습니다. 다른 좌석을 선택해 주세요.");
        }
    }

    public List<ReservedSeat> getReservedSeatsByScreeningId(Long screeningId) {
        return reservedSeatRepository.findByScreeningId(screeningId);
    }
}