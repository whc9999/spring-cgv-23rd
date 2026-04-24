package com.ceos23.cgv.domain.movie.controller.admin;

import com.ceos23.cgv.domain.movie.dto.ScreeningCreateRequest;
import com.ceos23.cgv.domain.movie.dto.ScreeningResponse;
import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.service.ScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/screenings")
@RequiredArgsConstructor
@Tag(name = "Admin Screening API", description = "관리자 전용 상영 일정 관리 API")
public class AdminScreeningController {

    private final ScreeningService screeningService;

    @PostMapping
    @Operation(summary = "상영 일정 등록", description = "특정 영화를 특정 상영관에 배치하여 상영 일정을 생성합니다.")
    public ResponseEntity<ScreeningResponse> createScreening(@RequestBody ScreeningCreateRequest request) {
        Screening screening = screeningService.createScreening(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ScreeningResponse.from(screening));
    }
}
