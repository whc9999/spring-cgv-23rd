package com.ceos23.cgv.domain.photo.repository;

import com.ceos23.cgv.domain.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}