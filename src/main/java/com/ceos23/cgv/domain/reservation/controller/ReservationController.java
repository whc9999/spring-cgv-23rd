package com.ceos23.cgv.domain.reservation.controller;

import com.ceos23.cgv.domain.reservation.dto.ReservationCreateRequest;
import com.ceos23.cgv.domain.reservation.dto.ReservationResponse;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.service.ReservationService;
import com.ceos23.cgv.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation API", description = "영화 예매 및 취소 API")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "영화 예매하기", description = "상영 일정, 인원수, 결제 수단, 좌석 정보를 입력받아 예매를 진행합니다.")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());

        Reservation reservation = reservationService.createReservation(
                userId,
                request.screeningId(),
                request.peopleCount(),
                request.payment(),
                request.couponCode(),
                request.seats()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ReservationResponse.from(reservation)));
    }

    @PatchMapping("/{reservationId}/cancel")
    @Operation(summary = "영화 예매 취소", description = "예매 ID와 유저 ID를 받아 본인의 예매 내역을 취소(CANCELED) 상태로 변경합니다.")
    public ResponseEntity<ApiResponse<String>> cancelReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        reservationService.cancelReservation(userId, reservationId);

        return ResponseEntity.ok(ApiResponse.success("예매가 성공적으로 취소되었습니다."));
    }
}
