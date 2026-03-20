package com.ceos23.cgv.domain.movie.repository;

import com.ceos23.cgv.domain.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    // 1. 전체 무비차트 (기존)
    List<Movie> findAllByOrderBySalesRateDesc();

    // 2. 현재 상영작 (개봉일이 오늘이거나 오늘보다 과거) - 예매율 내림차순
    List<Movie> findByReleaseDateLessThanEqualOrderBySalesRateDesc(LocalDate date);

    // 3. 상영 예정작 (개봉일이 오늘보다 미래) - 예매율 내림차순
    List<Movie> findByReleaseDateGreaterThanOrderBySalesRateDesc(LocalDate date);
}