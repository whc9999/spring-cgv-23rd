package com.ceos23.cgv.domain.user.service;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.repository.CinemaRepository;
import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.repository.MovieRepository;
import com.ceos23.cgv.domain.user.entity.Cinetalk;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.CinetalkRepository;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinetalkService {

    private final CinetalkRepository cinetalkRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;

    /**
     * [POST] 씨네톡 게시글 작성
     */
    @Transactional
    public Cinetalk createCinetalk(Long userId, String content, Long movieId, Long cinemaId) {
        // 1. 작성자(User) 조회 (필수)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 영화(Movie)와 극장(Cinema) 조회 (선택적)
        Movie movie = null;
        if (movieId != null) {
            movie = movieRepository.findById(movieId).orElse(null);
        }

        Cinema cinema = null;
        if (cinemaId != null) {
            cinema = cinemaRepository.findById(cinemaId).orElse(null);
        }

        // 3. 엔티티 생성 및 저장
        Cinetalk cinetalk = Cinetalk.builder()
                .user(user)
                .content(content)
                .likeCount(0)
                .movie(movie)
                .cinema(cinema)
                .build();

        return cinetalkRepository.save(cinetalk);
    }

    /**
     * [GET] 전체 씨네톡 게시글 조회
     */
    public List<Cinetalk> getAllCinetalks() {
        return cinetalkRepository.findAll();
    }

    /**
     * [GET] 특정 영화의 씨네톡 목록 조회
     */
    public List<Cinetalk> getCinetalksByMovieId(Long movieId) {
        return cinetalkRepository.findByMovieId(movieId);
    }

    /**
     * [GET] 특정 극장의 씨네톡 목록 조회
     */
    public List<Cinetalk> getCinetalksByCinemaId(Long cinemaId) {
        return cinetalkRepository.findByCinemaId(cinemaId);
    }
}