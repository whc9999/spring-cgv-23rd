package com.ceos23.cgv.domain.photo.repository;

import com.ceos23.cgv.domain.photo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 1. 특정 영화의 사진 목록 조회
    List<Photo> findByMovieId(Long movieId);

    // 2. 특정 인물(배우/감독)의 사진 목록 조회 추가
    List<Photo> findByPersonId(Long personId);
}