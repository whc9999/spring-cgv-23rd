package com.ceos23.cgv.domain.movie.repository;

import com.ceos23.cgv.domain.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    // 예매율(salesRate) 기준으로 내림차순 정렬하여 영화 목록 가져오기 (현재 상영작 차트용)
    List<Movie> findAllByOrderBySalesRateDesc();
}