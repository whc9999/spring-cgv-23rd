package com.ceos23.cgv.domain.event.repository;

import com.ceos23.cgv.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
