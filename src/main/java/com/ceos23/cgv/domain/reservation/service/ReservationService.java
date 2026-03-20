package com.ceos23.cgv.domain.reservation.service;

import com.ceos23.cgv.domain.cinema.enums.TheaterType;
import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.enums.Payment;
import com.ceos23.cgv.domain.reservation.enums.ReservationStatus;
import com.ceos23.cgv.domain.reservation.repository.ReservationRepository;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;

    /**
     * 영화 예매 로직
     */
    @Transactional
    public Reservation createReservation(Long userId, Long screeningId, int peopleCount, Payment payment, String couponCode) {
        // 1. 엔티티 조회 (유저와 상영일정 존재하는지)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCREENING_NOT_FOUND));

        // 2. 1인당 결제 금액 계산
        int ticketPrice = 15000; // 일반관 요금

        // 상영관 타입이 NORMAL이 아니면 특별관 요금 적용(20,000원으로 통일)
        if (screening.getTheater().getType() != TheaterType.NORMAL) {
            ticketPrice = 20000;
        }

        // 조조 영화인 경우 4,000원 할인
        // NullPointerException 방지를 위해 Boolean.TRUE.equals 사용
        if (Boolean.TRUE.equals(screening.getIsMorning())){
            ticketPrice -= 4000;
        }

        // 최종 금액 = 1인당 티켓 가격 * 예매 인원수
        int calculatedPrice = ticketPrice * peopleCount;

        // 3. 쿠폰 할인 적용
        if (couponCode != null && !couponCode.isBlank()) {
            calculatedPrice = applyCouponDiscount(calculatedPrice, couponCode);
        }

        // 3. 고유한 예매 번호 생성 (임시로 UUID 사용)
        String saleNumber = UUID.randomUUID().toString().substring(0, 15);

        // 4. 예매 엔티티 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .screening(screening)
                .status(ReservationStatus.COMPLETED)
                .peopleCount(peopleCount)
                .price(calculatedPrice)
                .payment(payment)
                .coupon(couponCode)
                .saleNumber(saleNumber)
                .build();

        // 5. DB에 저장
        return reservationRepository.save(reservation);
    }

    /**
     * 쿠폰 코드에 따른 할인 금액 계산 로직
     */
    private int applyCouponDiscount(int currentPrice, String couponCode) {
        int discountAmount = 0;

        // 임시 쿠폰 비즈니스 로직 (향후 Coupon 엔티티가 생기면 DB 조회로 변경 가능)
        if (couponCode.equals("WELCOME_CGV")){
            discountAmount = 3000; // 3,000원 할인
        } else if (couponCode.equals("VIP_HALF_PRICE")){
            discountAmount = currentPrice / 2; // 반값 할인
        } else {
            throw new IllegalArgumentException("유효하지 않은 쿠폰 코드입니다.");
        }

        // 할인 적용 (결제 금액이 0원 밑으로 떨어지지 않도록 방어 로직)
        int finalPrice = currentPrice - discountAmount;
        return Math.max(finalPrice, 0);
    }

    /**
     * 영화 예매 취소 로직
     */
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        // 1. 예매 내역 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예매 내역을 찾을 수 없습니다."));

        // 2. 권한 검증: 본인의 예매 내역이 맞는지 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예매 내역만 취소할 수 있습니다.");
        }

        // 3. 상태 검증: 이미 취소된 예매인지 확인
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("이미 취소 처리된 예매입니다.");
        }

        // 4. 취소 상태로 변경 (이후 @Transactional에 의해 DB에 자동 반영됨 - Dirty Checking)
        reservation.cancel();
    }
}