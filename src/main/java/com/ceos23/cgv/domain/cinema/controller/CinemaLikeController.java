package com.ceos23.cgv.domain.cinema.controller;

import com.ceos23.cgv.domain.cinema.dto.CinemaLikeResponse;
import com.ceos23.cgv.domain.cinema.service.CinemaLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cinema-likes")
@RequiredArgsConstructor
@Tag(name = "Cinema Like API", description = "자주 가는 극장(찜하기) 등록/취소 및 조회 API")
public class CinemaLikeController {

    private final CinemaLikeService cinemaLikeService;

    @PostMapping("/{cinemaId}")
    @Operation(summary = "자주 가는 극장 토글", description = "특정 극장을 자주 가는 극장으로 등록하거나 이미 등록되어 있다면 취소합니다.")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long cinemaId,
            @RequestParam Long userId) { // 임시로 쿼리 파라미터로 유저 ID 받음
        String resultMessage = cinemaLikeService.toggleCinemaLike(userId, cinemaId);
        return ResponseEntity.ok(resultMessage);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "내가 찜한 극장 목록 조회", description = "유저 ID를 통해 해당 유저가 자주 가는 극장으로 등록한 목록을 가져옵니다.")
    public ResponseEntity<List<CinemaLikeResponse>> getLikedCinemas(@PathVariable Long userId) {
        List<CinemaLikeResponse> responses = cinemaLikeService.getLikedCinemasByUser(userId).stream()
                .map(CinemaLikeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}