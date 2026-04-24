package com.ceos23.cgv.domain.reservation.service;

import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import com.ceos23.cgv.domain.payment.service.PaymentService;
import com.ceos23.cgv.domain.reservation.dto.ReservedSeatRequest;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.entity.ReservedSeat;
import com.ceos23.cgv.domain.reservation.enums.Payment;
import com.ceos23.cgv.domain.reservation.policy.ReservationPricePolicy;
import com.ceos23.cgv.domain.reservation.repository.ReservationRepository;
import com.ceos23.cgv.domain.reservation.repository.ReservedSeatRepository;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final ReservedSeatRepository reservedSeatRepository;
    private final PaymentService paymentService;
    private final TransactionTemplate transactionTemplate;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              ScreeningRepository screeningRepository,
                              ReservedSeatRepository reservedSeatRepository,
                              PaymentService paymentService,
                              PlatformTransactionManager transactionManager) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.screeningRepository = screeningRepository;
        this.reservedSeatRepository = reservedSeatRepository;
        this.paymentService = paymentService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 영화 예매 로직
     */
    public Reservation createReservation(Long userId, Long screeningId, int peopleCount, Payment payment,
                                         String couponCode, List<ReservedSeatRequest.SeatInfo> seats) {
        Reservation pendingReservation = transactionTemplate.execute(status ->
                createPendingReservation(userId, screeningId, peopleCount, payment, couponCode, seats)
        );

        try {
            paymentService.requestInstantPayment(pendingReservation);
        } catch (CustomException e) {
            cancelPendingReservation(pendingReservation.getPaymentId());
            throw e;
        } catch (RuntimeException e) {
            cancelPendingReservation(pendingReservation.getPaymentId());
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }

        try {
            return transactionTemplate.execute(status -> completeReservation(pendingReservation.getPaymentId()));
        } catch (RuntimeException e) {
            compensatePaidReservation(pendingReservation.getPaymentId());
            throw e;
        }
    }

    private Reservation createPendingReservation(Long userId, Long screeningId, int peopleCount, Payment payment,
                                                 String couponCode, List<ReservedSeatRequest.SeatInfo> seats) {
        User user = findUser(userId);
        Screening screening = findScreening(screeningId);
        validateSeats(peopleCount, seats);
        int calculatedPrice = ReservationPricePolicy.calculate(screening, peopleCount, couponCode);

        Reservation reservation = Reservation.create(
                user,
                screening,
                peopleCount,
                calculatedPrice,
                payment,
                couponCode,
                createSaleNumber(),
                paymentService.createPaymentId()
        );

        Reservation savedReservation = reservationRepository.save(reservation);
        saveReservedSeats(savedReservation, screening, seats);

        return savedReservation;
    }

    private Reservation completeReservation(String paymentId) {
        Reservation reservation = findReservationByPaymentId(paymentId);
        reservation.completePayment();
        return reservation;
    }

    private void cancelPendingReservation(String paymentId) {
        transactionTemplate.executeWithoutResult(status -> {
            Reservation reservation = findReservationByPaymentId(paymentId);
            reservedSeatRepository.deleteAllByReservationId(reservation.getId());
            reservation.cancel();
        });
    }

    private void compensatePaidReservation(String paymentId) {
        try {
            paymentService.cancelPayment(paymentId);
        } catch (RuntimeException e) {
            log.error("외부 결제 취소 보상 처리에 실패했습니다. paymentId={}", paymentId, e);
        }

        cancelPendingReservation(paymentId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Screening findScreening(Long screeningId) {
        return screeningRepository.findByIdForUpdate(screeningId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCREENING_NOT_FOUND));
    }

    private Reservation findReservationByPaymentId(String paymentId) {
        return reservationRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateSeats(int peopleCount, List<ReservedSeatRequest.SeatInfo> seats) {
        if (seats == null || seats.isEmpty() || seats.size() != peopleCount) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
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
    public void cancelReservation(Long userId, Long reservationId) {
        String paymentId = transactionTemplate.execute(status ->
                getCancelablePaymentId(userId, reservationId)
        );

        paymentService.cancelPayment(paymentId);

        transactionTemplate.executeWithoutResult(status ->
                cancelPaidReservation(userId, reservationId)
        );
    }

    private String getCancelablePaymentId(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.validateCancelableBy(userId);
        reservation.validatePaymentCompleted();

        return reservation.getPaymentId();
    }

    private void cancelPaidReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.validateCancelableBy(userId);
        reservation.validatePaymentCompleted();
        reservedSeatRepository.deleteAllByReservationId(reservationId);

        reservation.cancel();
    }
}
