package com.ceos23.cgv.domain.reservation.entity;

import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.reservation.enums.Payment;
import com.ceos23.cgv.domain.reservation.enums.ReservationStatus;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private int peopleCount; //누적 관객 수

    @Column(nullable = false)
    private int price; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Payment payment;

    @Column(length = 50)
    private String coupon;

    @Column(nullable = false, unique = true, length = 50)
    private String saleNumber; // 예매 번호

    // 예매 취소 메서드
    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }
}
