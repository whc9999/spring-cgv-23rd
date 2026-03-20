package com.ceos23.cgv.domain.event.repository;

import com.ceos23.cgv.domain.event.entity.MovieEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieEventRepository extends JpaRepository<MovieEvent, Long> {
    // 특정 영화와 연관된 이벤트 목록 조회
    List<MovieEvent> findByMovieId(Long movieId);
}