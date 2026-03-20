package com.ceos23.cgv.domain.movie.controller;

import com.ceos23.cgv.domain.movie.dto.MovieCreateRequest;
import com.ceos23.cgv.domain.movie.dto.MovieResponse;
import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movie API", description = "영화 관련 생성, 조회, 삭제 API")
public class MovieController {

    private final MovieService movieService;

    // 1. 새로운 데이터를 Create (POST)
    @PostMapping
    @Operation(summary = "새로운 영화 등록", description = "영화 정보를 입력받아 DB에 생성합니다.")
    public ResponseEntity<MovieResponse> createMovie(@RequestBody MovieCreateRequest request) {
        Movie createdMovie = movieService.createMovie(
                request.title(),
                request.runningTime(),
                request.releaseDate(),
                request.movieRating(),
                request.genre(),
                request.prologue()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovieResponse.from(createdMovie));
    }

    // 모든 데이터를 가져오기 (GET)
    @GetMapping
    @Operation(summary = "전체 영화 조회", description = "DB에 저장된 모든 영화 목록을 가져옵니다.")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        List<MovieResponse> responses= movieService.getAllMovies().stream()
                .map(MovieResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // 3. 특정 데이터를 가져오기 (GET)
    @GetMapping("/{movieId}")
    @Operation(summary = "특정 영화 단건 조회", description = "영화 ID(PK)를 통해 영화 하나의 상세 정보를 가져옵니다.")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long movieId) {
        Movie movie = movieService.getMovieDetails(movieId);
        return ResponseEntity.ok(MovieResponse.from(movie));
    }

    // 4. 특정 데이터를 삭제하기 (DELETE)
    @DeleteMapping("/{movieId}")
    @Operation(summary = "특정 영화 삭제", description = "영화 ID를 통해 특정 영화를 DB에서 삭제합니다.")
    public ResponseEntity<String> deleteMovie(@PathVariable Long movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.ok("영화가 성공적으로 삭제되었습니다.");
    }
}
