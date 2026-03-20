package com.ceos23.cgv.domain.person.repository;

import com.ceos23.cgv.domain.person.entity.WorkParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkParticipationRepository extends JpaRepository<WorkParticipation, Long> {
}