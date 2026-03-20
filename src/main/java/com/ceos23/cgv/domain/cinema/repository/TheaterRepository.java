package com.ceos23.cgv.domain.cinema.repository;

import com.ceos23.cgv.domain.cinema.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheaterRepository extends JpaRepository<Theater, Long> {
    // 특정 지점(Cinema)에 속한 상영관 목록 조회
    List<Theater> findByCinemaId(Long cinemaId);
}