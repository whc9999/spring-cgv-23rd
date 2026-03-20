package com.ceos23.cgv.domain.cinema.repository;

import com.ceos23.cgv.domain.cinema.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}