package com.ceos23.cgv.domain.user.controller;

import com.ceos23.cgv.domain.user.dto.CinetalkCreateRequest;
import com.ceos23.cgv.domain.user.dto.CinetalkResponse;
import com.ceos23.cgv.domain.user.entity.Cinetalk;
import com.ceos23.cgv.domain.user.service.CinetalkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cinetalks")
@RequiredArgsConstructor
@Tag(name = "Cinetalk API", description = "씨네톡(커뮤니티) 게시글 작성 및 조회 API")
public class CinetalkController {

    private final CinetalkService cinetalkService;

    @PostMapping
    @Operation(summary = "씨네톡 게시글 작성", description = "영화나 극장을 선택(혹은 생략)하여 자유게시글을 작성합니다.")
    public ResponseEntity<CinetalkResponse> createCinetalk(@RequestBody CinetalkCreateRequest request) {
        Cinetalk cinetalk = cinetalkService.createCinetalk(
                request.userId(),
                request.content(),
                request.movieId(),
                request.cinemaId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CinetalkResponse.from(cinetalk));
    }

    @GetMapping
    @Operation(summary = "씨네톡 전체 목록 조회", description = "작성된 모든 씨네톡 게시글을 조회합니다.")
    public ResponseEntity<List<CinetalkResponse>> getAllCinetalks() {
        List<CinetalkResponse> responses = cinetalkService.getAllCinetalks().stream()
                .map(CinetalkResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "특정 영화의 씨네톡 조회")
    public ResponseEntity<List<CinetalkResponse>> getCinetalksByMovie(@PathVariable Long movieId) {
        List<CinetalkResponse> responses = cinetalkService.getCinetalksByMovieId(movieId).stream()
                .map(CinetalkResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/cinema/{cinemaId}")
    @Operation(summary = "특정 극장의 씨네톡 조회", description = "특정 극장에 작성된 씨네톡 게시글 목록을 조회합니다.")
    public ResponseEntity<List<CinetalkResponse>> getCinetalksByCinema(@PathVariable Long cinemaId) {
        List<CinetalkResponse> responses = cinetalkService.getCinetalksByCinemaId(cinemaId).stream()
                .map(CinetalkResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}