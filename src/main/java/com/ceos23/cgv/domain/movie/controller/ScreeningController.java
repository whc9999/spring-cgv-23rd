package com.ceos23.cgv.domain.movie.controller;

import com.ceos23.cgv.domain.movie.dto.ScreeningResponse;
import com.ceos23.cgv.domain.movie.service.ScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "특정 영화의 시간표 조회", description = "영화 ID를 통해 해당 영화의 모든 상영 일정(어느 지점, 몇 시 상영 등)을 조회합니다.")
    public ResponseEntity<List<ScreeningResponse>> getScreeningsByMovie(@PathVariable Long movieId) {
        List<ScreeningResponse> responses = screeningService.getScreeningsByMovieId(movieId).stream()
                .map(ScreeningResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
