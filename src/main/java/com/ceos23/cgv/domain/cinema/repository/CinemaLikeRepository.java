package com.ceos23.cgv.domain.cinema.repository;

import com.ceos23.cgv.domain.cinema.entity.CinemaLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CinemaLikeRepository extends JpaRepository<CinemaLike, Long> {
}