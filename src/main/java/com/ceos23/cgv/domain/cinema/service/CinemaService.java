package com.ceos23.cgv.domain.cinema.service;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.entity.Theater;
import com.ceos23.cgv.domain.cinema.repository.CinemaRepository;
import com.ceos23.cgv.domain.cinema.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final TheaterRepository theaterRepository;

    /**
     * 전체 영화관(지점) 목록 조회
     */
    public List<Cinema> getAllCinema() {
        return cinemaRepository.findAll();
    }

    /**
     * 단일 영화관(지점) 상세 조회
     */
    public Cinema getCinemaDetails(Long cinemaId) {
        return cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 영화관을 찾을 수 없습니다."));
    }

    /**
     * 특정 영화관의 전체 상영관(Theater) 목록 조회
     */
    public List<Theater> getTheatersByCinemaId(Long cinemaId) {
        // 먼저 영화관이 존재하는지 검증
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new IllegalArgumentException("해당 영화관을 찾을 수 없습니다.");
        }
        return theaterRepository.findByCinemaId(cinemaId);
    }
}
