package com.ceos23.cgv.domain.movie.controller;

import com.ceos23.cgv.domain.movie.dto.MovieLikeResponse;
import com.ceos23.cgv.domain.movie.service.MovieLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movie-likes")
@RequiredArgsConstructor
@Tag(name = "Movie Like API", description = "영화 찜하기 등록/취소 및 조회 API")
public class MovieLikeController {

    private final MovieLikeService movieLikeService;

    @PostMapping("/{movieId}")
    @Operation(summary = "영화 찜 토글", description = "특정 영화에 대해 찜 누르거나 이미 눌려있다면 취소합니다.")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long movieId,
            @RequestParam Long userId) { // 임시로 쿼리 파라미터로 유저 ID 받음
        String resultMessage = movieLikeService.toggleMovieLike(userId, movieId);
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "내가 찜 누른 영화 목록 조회", description = "유저 ID를 통해 해당 유저가 찜한 영화 목록을 가져옵니다.")
    public ResponseEntity<List<MovieLikeResponse>> getLikedMovies(@PathVariable Long userId) {
        List<MovieLikeResponse> responses = movieLikeService.getLikedMoviesByUser(userId).stream()
                .map(MovieLikeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}