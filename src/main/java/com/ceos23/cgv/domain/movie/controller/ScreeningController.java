package com.ceos23.cgv.domain.movie.controller;

import com.ceos23.cgv.domain.movie.dto.ScreeningCreateRequest;
import com.ceos23.cgv.domain.movie.dto.ScreeningResponse;
import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.service.ScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
@Tag(name = "Screening API", description = "상영 일정(시간표) 관리 API")
public class ScreeningController {

    private final ScreeningService screeningService;

    @PostMapping
    @Operation(summary = "상영 일정 등록", description = "특정 영화를 특정 상영관에 배치하여 상영 일정을 생성합니다.")
    public ResponseEntity<ScreeningResponse> createScreening(@RequestBody ScreeningCreateRequest request) {
        Screening screening = screeningService.createScreening(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ScreeningResponse.from(screening));
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "특정 영화의 시간표 조회", description = "영화 ID를 통해 해당 영화의 모든 상영 일정(어느 지점, 몇 시 상영 등)을 조회합니다.")
    public ResponseEntity<List<ScreeningResponse>> getScreeningsByMovie(@PathVariable Long movieId) {
        List<ScreeningResponse> responses = screeningService.getScreeningsByMovieId(movieId).stream()
                .map(ScreeningResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}