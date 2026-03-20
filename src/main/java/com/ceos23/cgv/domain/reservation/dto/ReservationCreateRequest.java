package com.ceos23.cgv.domain.reservation.dto;

import com.ceos23.cgv.domain.reservation.enums.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {
    private Long userId;        // 임시: 어떤 유저가 예매하는지
    private Long screeningId;   // 어떤 상영 일정(영화+상영관+시간)을 예매하는지
    private int peopleCount;    // 몇 명인지
    private Payment payment;    // 결제 수단 (예: KAKAO_PAY, CREDIT_CARD 등)
    private String couponCode;  // 쿠폰 코드 (없으면 null 또는 빈 문자열)
}