package com.ceos23.cgv.domain.movie.repository;

import com.ceos23.cgv.domain.movie.entity.MovieLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieLikeRepository extends JpaRepository<MovieLike, Long> {
}