package com.ceos23.cgv.domain.movie.repository;

import com.ceos23.cgv.domain.movie.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    // 특정 영화의 상영 일정만 가져오는 모두 가져오는 메서드
    List<Screening> findByMovieId(Long movieId);
}