package com.ceos23.cgv.domain.user.repository;

import com.ceos23.cgv.domain.user.entity.Cinetalk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinetalkRepository extends JpaRepository<Cinetalk, Long> {

    // 영화 ID로 씨네톡 목록 찾기
    List<Cinetalk> findByMovieId(Long movieId);

    // 극장 ID로 씨네톡 목록 찾기
    List<Cinetalk> findByCinemaId(Long cinemaId);
}