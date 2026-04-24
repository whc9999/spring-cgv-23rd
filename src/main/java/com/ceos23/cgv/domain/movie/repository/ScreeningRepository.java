package com.ceos23.cgv.domain.movie.repository;

import com.ceos23.cgv.domain.movie.entity.Screening;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    // 특정 영화의 상영 일정만 가져오는 모두 가져오는 메서드
    List<Screening> findByMovieId(Long movieId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Screening s where s.id = :screeningId")
    Optional<Screening> findByIdForUpdate(@Param("screeningId") Long screeningId);
}
