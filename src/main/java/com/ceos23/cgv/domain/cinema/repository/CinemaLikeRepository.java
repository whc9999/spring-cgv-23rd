package com.ceos23.cgv.domain.cinema.repository;

import com.ceos23.cgv.domain.cinema.entity.CinemaLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CinemaLikeRepository extends JpaRepository<CinemaLike, Long> {
    // 유저 ID와 극장 ID로 기존에 찜했는지 찾는 메서드
    Optional<CinemaLike> findByUserIdAndCinemaId(Long userId, Long cinemaId);

    // 특정 유저가 찜한 극장 목록만 모아보기
    List<CinemaLike> findByUserId(Long userId);
}