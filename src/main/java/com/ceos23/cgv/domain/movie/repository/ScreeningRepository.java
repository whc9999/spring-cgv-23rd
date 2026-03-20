package com.ceos23.cgv.domain.movie.repository;

import com.ceos23.cgv.domain.movie.entity.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    // 특정 영화의 상영 일정만 가져오는 커스텀 메서드 예시
    // List<Screening> findByMovieId(Long movieId);
}