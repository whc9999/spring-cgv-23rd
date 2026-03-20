package com.ceos23.cgv.domain.reservation.repository;

import com.ceos23.cgv.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 예매 번호로 예매 내역 조회
    Optional<Reservation> findBySaleNumber(String saleNumber);
}