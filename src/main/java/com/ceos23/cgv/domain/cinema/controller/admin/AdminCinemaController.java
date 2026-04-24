package com.ceos23.cgv.domain.cinema.controller.admin;

import com.ceos23.cgv.domain.cinema.dto.CinemaCreateRequest;
import com.ceos23.cgv.domain.cinema.dto.CinemaResponse;
import com.ceos23.cgv.domain.cinema.dto.TheaterCreateRequest;
import com.ceos23.cgv.domain.cinema.dto.TheaterResponse;
import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.entity.Theater;
import com.ceos23.cgv.domain.cinema.service.CinemaService;
import com.ceos23.cgv.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
@Tag(name = "Admin Cinema API", description = "관리자 전용 영화관 및 상영관 관리 API")
public class AdminCinemaController {

    private final CinemaService cinemaService;

    @PostMapping
    @Operation(summary = "영화관(지점) 생성", description = "새로운 CGV 지점(예: 강남점)을 등록합니다.")
    public ResponseEntity<ApiResponse<CinemaResponse>> createCinema(@RequestBody CinemaCreateRequest request) {
        Cinema createdCinema = cinemaService.createCinema(
                request.name(),
                request.region()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(CinemaResponse.from(createdCinema)));
    }

    @PostMapping("/{cinemaId}/theaters")
    @Operation(summary = "상영관 생성", description = "특정 영화관 지점에 새로운 상영관(예: 1관, IMAX관)을 등록합니다.")
    public ResponseEntity<ApiResponse<TheaterResponse>> createTheater(
            @PathVariable Long cinemaId,
            @RequestBody TheaterCreateRequest request) {

        Theater createdTheater = cinemaService.createTheater(cinemaId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(TheaterResponse.from(createdTheater)));
    }
}
