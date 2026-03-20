package com.ceos23.cgv.domain.reservation.repository;

import com.ceos23.cgv.domain.reservation.entity.ReservedSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservedSeatRepository extends JpaRepository<ReservedSeat, Long> {
}