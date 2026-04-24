package com.ceos23.cgv.domain.cinema.controller;

import com.ceos23.cgv.domain.cinema.dto.CinemaResponse;
import com.ceos23.cgv.domain.cinema.dto.TheaterResponse;
import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.entity.Theater;
import com.ceos23.cgv.domain.cinema.service.CinemaService;
import com.ceos23.cgv.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
@Tag(name = "Cinema API", description = "영화관(지점) 및 상영관 조회 API")
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    @Operation(summary = "전체 영화관 목록 조회", description = "CGV의 모든 지점 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CinemaResponse>>> getAllCinemas() {
        List<Cinema> cinemas = cinemaService.getAllCinema();
        return ResponseEntity.ok(ApiResponse.success(cinemas, CinemaResponse::from));
    }

    @GetMapping("/{cinemaId}")
    @Operation(summary = "특정 영화관 단건 조회", description = "영화관 ID를 통해 특정 지점의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<CinemaResponse>> getCinemaById(@PathVariable Long cinemaId) {
        Cinema cinema = cinemaService.getCinemaDetails(cinemaId);
        return ResponseEntity.ok(ApiResponse.success(CinemaResponse.from(cinema)));
    }

    @GetMapping("/{cinemaId}/theaters")
    @Operation(summary = "특정 영화관의 상영관 목록 조회", description = "영화관 ID를 통해 해당 지점에 속한 모든 상영관(1관, IMAX관 등) 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TheaterResponse>>> getTheatersByCinemaId(@PathVariable Long cinemaId) {
        List<Theater> theaters = cinemaService.getTheatersByCinemaId(cinemaId);
        return ResponseEntity.ok(ApiResponse.success(theaters, TheaterResponse::from));
    }

}
