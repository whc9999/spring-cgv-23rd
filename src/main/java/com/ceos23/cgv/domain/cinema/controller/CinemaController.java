package com.ceos23.cgv.domain.cinema.controller;

import com.ceos23.cgv.domain.cinema.dto.CinemaCreateRequest;
import com.ceos23.cgv.domain.cinema.dto.CinemaResponse;
import com.ceos23.cgv.domain.cinema.dto.TheaterCreateRequest;
import com.ceos23.cgv.domain.cinema.dto.TheaterResponse;
import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.entity.Theater;
import com.ceos23.cgv.domain.cinema.service.CinemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
@Tag(name = "Cinema API", description = "영화관(지점) 및 상영관 조회 API")
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    @Operation(summary = "전체 영화관 목록 조회", description = "CGV의 모든 지점 목록을 조회합니다.")
    public ResponseEntity<List<CinemaResponse>> getAllCinemas() {
        List<CinemaResponse> responses = cinemaService.getAllCinema().stream()
                .map(CinemaResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{cinemaId}")
    @Operation(summary = "특정 영화관 단건 조회", description = "영화관 ID를 통해 특정 지점의 상세 정보를 조회합니다.")
    public ResponseEntity<CinemaResponse> getCinemaById(@PathVariable Long cinemaId) {
        Cinema cinema = cinemaService.getCinemaDetails(cinemaId);
        return ResponseEntity.ok(CinemaResponse.from(cinema));
    }

    @GetMapping("/{cinemaId}/theaters")
    @Operation(summary = "특정 영화관의 상영관 목록 조회", description = "영화관 ID를 통해 해당 지점에 속한 모든 상영관(1관, IMAX관 등) 목록을 조회합니다.")
    public ResponseEntity<List<TheaterResponse>> getTheatersByCinemaId(@PathVariable Long cinemaId) {
        List<TheaterResponse> responses = cinemaService.getTheatersByCinemaId(cinemaId).stream()
                .map(TheaterResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // 4. 영화관(지점) 생성 (POST)
    @PostMapping
    @Operation(summary = "영화관(지점) 생성", description = "새로운 CGV 지점(예: 강남점)을 등록합니다.")
    public ResponseEntity<CinemaResponse> createCinema(@RequestBody CinemaCreateRequest request) {
        Cinema createdCinema = cinemaService.createCinema(
                request.name(),
                request.region()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CinemaResponse.from(createdCinema));
    }

    // 5. 상영관 생성 (POST)
    @PostMapping("/{cinemaId}/theaters")
    @Operation(summary = "상영관 생성", description = "특정 영화관 지점에 새로운 상영관(예: 1관, IMAX관)을 등록합니다.")
    public ResponseEntity<TheaterResponse> createTheater(@RequestBody TheaterCreateRequest request) {
        Theater createdTheater = cinemaService.createTheater(
                request.cinemaId(),
                request.name(),
                request.type(),
                request.maxRow(),
                request.maxCol()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TheaterResponse.from(createdTheater));
    }
}