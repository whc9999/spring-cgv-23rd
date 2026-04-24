package com.ceos23.cgv.domain.reservation.service;

import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import com.ceos23.cgv.domain.reservation.dto.ReservedSeatRequest;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.repository.ReservationRepository;
import com.ceos23.cgv.domain.reservation.repository.ReservedSeatRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservedSeatServiceTest {

    @Mock
    private ReservedSeatRepository reservedSeatRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ScreeningRepository screeningRepository;

    @InjectMocks
    private ReservedSeatService reservedSeatService;

    @Test
    @DisplayName("이미 예매된 좌석 선택 시 SEAT_ALREADY_RESERVED 예외가 발생한다")
    void createReservedSeats_Fail_AlreadyReserved() {
        // Given (준비)
        Long reservationId = 1L;
        Long screeningId = 1L;
        // G4, G5 좌석을 예매하려는 요청 생성
        ReservedSeatRequest request = new ReservedSeatRequest(
                reservationId, screeningId,
                List.of(new ReservedSeatRequest.SeatInfo("G", 4), new ReservedSeatRequest.SeatInfo("G", 5))
        );

        Reservation reservation = Reservation.builder().id(reservationId).build();
        Screening screening = Screening.builder().id(screeningId).build();

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(screeningRepository.findByIdForUpdate(screeningId)).willReturn(Optional.of(screening));

        given(reservedSeatRepository.saveAll(anyList())).willThrow(DataIntegrityViolationException.class);

        // When (실행) & Then (검증)
        CustomException exception = assertThrows(CustomException.class, () -> {
            reservedSeatService.createReservedSeats(request);
        });

        // 의도한 대로 중복 예매 에러코드(R002)로 잘 변환되어 터지는지 확인
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SEAT_ALREADY_RESERVED);
    }
}
