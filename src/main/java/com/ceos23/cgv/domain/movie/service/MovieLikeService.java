package com.ceos23.cgv.domain.movie.service;

import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.entity.MovieLike;
import com.ceos23.cgv.domain.movie.repository.MovieLikeRepository;
import com.ceos23.cgv.domain.movie.repository.MovieRepository;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieLikeService {

    private final MovieLikeRepository movieLikeRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    /**
     * [POST] 영화 찜 토글 (없으면 생성, 있으면 삭제)
     */
    @Transactional
    public String toggleMovieLike(Long userId, Long movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomException(ErrorCode.MOVIE_NOT_FOUND));

        // 기존에 찜을 눌렀는지 확인
        Optional<MovieLike> existingLike = movieLikeRepository.findByUserIdAndMovieId(userId, movieId);

        if (existingLike.isPresent()) {
            // 이미 누른 상태라면 -> 찜 취소 (삭제)
            movieLikeRepository.delete(existingLike.get());
            return "찜이 취소되었습니다.";
        } else {
            // 안 누른 상태라면 -> 찜 생성 (저장)
            MovieLike newLike = MovieLike.builder()
                    .user(user)
                    .movie(movie)
                    .build();
            movieLikeRepository.save(newLike);
            return "찜이 추가되었습니다.";
        }
    }

    /**
     * [GET] 특정 유저가 찜한 영화 목록 조회
     */
    public List<MovieLike> getLikedMoviesByUser(Long userId) {
        return movieLikeRepository.findByUserId(userId);
    }
}