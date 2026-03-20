package com.ceos23.cgv.domain.movie.service;

import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;

    /**
     * 1. 무비차트 전체 (예매율 순)
     */
    public List<Movie> getMoviesBySalesRate() {
        return movieRepository.findAllByOrderBySalesRateDesc();
    }

    /**
     * 2. 현재 상영작 탭 (이미 개봉한 영화들을 예매율 순으로 정렬)
     */
    public List<Movie> getCurrentlyScreeningMovies() {
        // 오늘 날짜를 기준으로 오늘 이전(오늘 포함)에 개봉한 영화들만 조회
        LocalDate today = LocalDate.now();
        return movieRepository.findByReleaseDateLessThanEqualOrderBySalesRateDesc(today);
    }

    /**
     * 3. 상영 예정작 탭 (아직 개봉하지 않은 영화들을 예매율 순으로 정렬)
     */
    public List<Movie> getUpcomingMovies() {
        // 오늘 날짜를 기준으로 내일 이후에 개봉할 영화들만 조회
        LocalDate today = LocalDate.now();
        return movieRepository.findByReleaseDateGreaterThanOrderBySalesRateDesc(today);
    }

    /**
     * 4. 특정 영화 상세 정보 조회
     */
    public Movie getMovieDetails(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("해당 영화를 찾을 수 없습니다."));
    }
}