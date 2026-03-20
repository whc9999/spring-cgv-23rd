package com.ceos23.cgv.domain.movie.repository;

import com.ceos23.cgv.domain.movie.entity.MovieLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieLikeRepository extends JpaRepository<MovieLike, Long> {
    // 유저 ID와 영화 ID로 기존에 찜을 눌렀는지 찾는 메서드
    Optional<MovieLike> findByUserIdAndMovieId(Long userId, Long movieId);

    // 특정 유저가 찜 누른 영화 목록만 모아보기
    List<MovieLike> findByUserId(Long userId);
}