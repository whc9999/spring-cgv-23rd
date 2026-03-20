package com.ceos23.cgv.domain.person.repository;

import com.ceos23.cgv.domain.person.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}