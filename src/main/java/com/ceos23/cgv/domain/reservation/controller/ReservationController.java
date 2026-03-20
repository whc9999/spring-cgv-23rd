package com.ceos23.cgv.domain.reservation.controller;

import com.ceos23.cgv.domain.reservation.dto.ReservationCreateRequest;
import com.ceos23.cgv.domain.reservation.dto.ReservationResponse;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation API", description = "영화 예매 및 취소 API")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "영화 예매하기", description = "유저, 상영 일정, 인원수, 결제 수단 등을 입력받아 예매를 진행합니다.")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationCreateRequest request) {

        Reservation reservation = reservationService.createReservation(
                request.userId(),
                request.screeningId(),
                request.peopleCount(),
                request.payment(),
                request.couponCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @PatchMapping("/{reservationId}/cancel")
    @Operation(summary = "영화 예매 취소", description = "예매 ID와 유저 ID를 받아 본인의 예매 내역을 취소(CANCELED) 상태로 변경합니다.")
    public ResponseEntity<String> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam Long userId) { // 임시로 쿼리 파라미터로 userId를 받음

        reservationService.cancelReservation(userId, reservationId);

        return ResponseEntity.ok("예매가 성공적으로 취소되었습니다.");
    }
}