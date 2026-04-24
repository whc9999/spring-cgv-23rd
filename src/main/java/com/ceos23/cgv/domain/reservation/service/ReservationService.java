package com.ceos23.cgv.domain.reservation.service;

import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import com.ceos23.cgv.domain.reservation.dto.ReservedSeatRequest;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.entity.ReservedSeat;
import com.ceos23.cgv.domain.reservation.enums.Payment;
import com.ceos23.cgv.domain.reservation.policy.CouponDiscountPolicy;
import com.ceos23.cgv.domain.reservation.repository.ReservationRepository;
import com.ceos23.cgv.domain.reservation.repository.ReservedSeatRepository;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final ReservedSeatRepository reservedSeatRepository;

    // 전역적으로 관리할 할인 금액 상수 선언
    private static final int MORNING_DISCOUNT = 4000;

    /**
     * 영화 예매 로직
     */
    @Transactional
    public Reservation createReservation(Long userId, Long screeningId, int peopleCount, Payment payment,
                                         String couponCode, List<ReservedSeatRequest.SeatInfo> seats) {
        User user = findUser(userId);
        Screening screening = findScreening(screeningId);
        validateSeats(peopleCount, seats);
        int calculatedPrice = calculatePrice(screening, peopleCount, couponCode);

        Reservation reservation = Reservation.create(
                user,
                screening,
                peopleCount,
                calculatedPrice,
                payment,
                couponCode,
                createSaleNumber()
        );

        Reservation savedReservation = reservationRepository.save(reservation);
        saveReservedSeats(savedReservation, screening, seats);

        return savedReservation;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Screening findScreening(Long screeningId) {
        return screeningRepository.findByIdForUpdate(screeningId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCREENING_NOT_FOUND));
    }

    private void validateSeats(int peopleCount, List<ReservedSeatRequest.SeatInfo> seats) {
        if (seats == null || seats.isEmpty() || seats.size() != peopleCount) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private int calculatePrice(Screening screening, int peopleCount, String couponCode) {
        int ticketPrice = calculateTicketPrice(screening);
        int calculatedPrice = ticketPrice * peopleCount;

        return CouponDiscountPolicy.apply(calculatedPrice, couponCode);
    }

    private int calculateTicketPrice(Screening screening) {
        int ticketPrice = screening.getTheater().getType().getBasePrice();

        if (Boolean.TRUE.equals(screening.getIsMorning())) {
            return ticketPrice - MORNING_DISCOUNT;
        }

        return ticketPrice;
    }

    private String createSaleNumber() {
        return UUID.randomUUID().toString().substring(0, 15);
    }

    private void saveReservedSeats(Reservation reservation, Screening screening,
                                   List<ReservedSeatRequest.SeatInfo> seats) {
        List<ReservedSeat> reservedSeats = seats.stream()
                .map(seatInfo -> ReservedSeat.create(
                        reservation,
                        screening,
                        seatInfo.row(),
                        seatInfo.col()
                ))
                .toList();

        try {
            reservedSeatRepository.saveAll(reservedSeats);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
    }

    /**
     * 영화 예매 취소 로직
     */
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.validateCancelableBy(userId);

        reservedSeatRepository.deleteAllByReservationId(reservationId);

        reservation.cancel();
    }
}
