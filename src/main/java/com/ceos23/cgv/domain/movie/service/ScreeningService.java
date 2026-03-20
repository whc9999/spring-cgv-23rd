package com.ceos23.cgv.domain.movie.service;

import com.ceos23.cgv.domain.cinema.entity.Theater;
import com.ceos23.cgv.domain.cinema.repository.TheaterRepository;
import com.ceos23.cgv.domain.movie.dto.ScreeningCreateRequest;
import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.MovieRepository;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;

    /**
     * [POST] 새로운 상영 일정 등록
     */
    @Transactional
    public Screening createScreening(ScreeningCreateRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다."));

        Theater theater = theaterRepository.findById(request.theaterId())
                .orElseThrow(() -> new IllegalArgumentException("상영관을 찾을 수 없습니다."));

        Screening screening = Screening.builder()
                .movie(movie)
                .theater(theater)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .isMorning(request.isMorning())
                .build();

        return screeningRepository.save(screening);
    }

    /**
     * [GET] 특정 영화의 상영 일정(시간표) 조회
     */
    public List<Screening> getScreeningsByMovieId(Long movieId) {
        return screeningRepository.findByMovieId(movieId);
    }
}