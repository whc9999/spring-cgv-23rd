package com.ceos23.cgv.domain.event.repository;

import com.ceos23.cgv.domain.event.entity.MovieEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieEventRepository extends JpaRepository<MovieEvent, Long> {
}