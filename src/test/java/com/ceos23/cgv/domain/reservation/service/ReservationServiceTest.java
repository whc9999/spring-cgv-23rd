package com.ceos23.cgv.domain.reservation.service;

import com.ceos23.cgv.domain.cinema.entity.Theater;
import com.ceos23.cgv.domain.cinema.enums.TheaterType;
import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.entity.Screening;
import com.ceos23.cgv.domain.movie.repository.ScreeningRepository;
import com.ceos23.cgv.domain.reservation.dto.ReservedSeatRequest;
import com.ceos23.cgv.domain.reservation.entity.Reservation;
import com.ceos23.cgv.domain.reservation.enums.Payment;
import com.ceos23.cgv.domain.reservation.repository.ReservationRepository;
import com.ceos23.cgv.domain.reservation.repository.ReservedSeatRepository;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservedSeatRepository reservedSeatRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ScreeningRepository screeningRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("정상적으로 예매 정보가 생성되고 저장된다")
    void createReservation_Success() {
        // Given
        Long userId = 1L;
        Long screeningId = 1L;
        int peopleCount = 2;
        Payment payment = Payment.APP_CARD;
        String couponCode = null;
        List<ReservedSeatRequest.SeatInfo> seats = List.of(
                new ReservedSeatRequest.SeatInfo("G", 4),
                new ReservedSeatRequest.SeatInfo("G", 5)
        );

        User user = User.builder().id(userId).nickname("우혁").build();

        // 💡 영화와 상영관 객체도 가짜로 만들어 줍니다.
        Movie movie = Movie.builder().id(1L).title("테스트 영화").build();
        // TheaterType 임포트 필요: import com.ceos23.cgv.domain.cinema.enums.TheaterType;
        Theater theater = Theater.builder().id(1L).name("1관").type(TheaterType.NORMAL).build();

        // 💡 텅 빈 Screening이 아니라, 꽉 찬 Screening으로 조립해 줍니다!
        Screening screening = Screening.builder()
                .id(screeningId)
                .movie(movie)       // 추가
                .theater(theater)   // 추가
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(screeningRepository.findByIdForUpdate(screeningId)).willReturn(Optional.of(screening));

        // save 될 때 들어온 엔티티 그대로 반환
        given(reservationRepository.save(any(Reservation.class))).willAnswer(i -> i.getArgument(0));

        // When
        Reservation reservation = reservationService.createReservation(userId, screeningId, peopleCount, payment, couponCode, seats);

        // Then
        assertThat(reservation.getUser().getNickname()).isEqualTo("우혁");
        assertThat(reservation.getPeopleCount()).isEqualTo(2);
        assertThat(reservation.getPayment()).isEqualTo(Payment.APP_CARD);
        verify(reservationRepository).save(any(Reservation.class));
        verify(reservedSeatRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("WELCOME_CGV 쿠폰을 적용하면 총 예매 금액에서 3000원이 할인된다")
    void createReservation_Success_WelcomeCouponDiscount() {
        // Given
        Long userId = 1L;
        Long screeningId = 1L;
        User user = User.builder().id(userId).nickname("우혁").build();
        Screening screening = createScreening(screeningId, TheaterType.NORMAL);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(screeningRepository.findByIdForUpdate(screeningId)).willReturn(Optional.of(screening));
        given(reservationRepository.save(any(Reservation.class))).willAnswer(i -> i.getArgument(0));

        // When
        Reservation reservation = reservationService.createReservation(
                userId,
                screeningId,
                2,
                Payment.APP_CARD,
                "WELCOME_CGV",
                List.of(
                        new ReservedSeatRequest.SeatInfo("G", 4),
                        new ReservedSeatRequest.SeatInfo("G", 5)
                )
        );

        // Then
        assertThat(reservation.getPrice()).isEqualTo(27000);
    }

    @Test
    @DisplayName("잘못된 쿠폰 코드로 예매하면 INVALID_COUPON_CODE 예외가 발생한다")
    void createReservation_Fail_InvalidCouponCode() {
        // Given
        Long userId = 1L;
        Long screeningId = 1L;
        User user = User.builder().id(userId).nickname("우혁").build();
        Screening screening = createScreening(screeningId, TheaterType.NORMAL);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(screeningRepository.findByIdForUpdate(screeningId)).willReturn(Optional.of(screening));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            reservationService.createReservation(
                    userId,
                    screeningId,
                    2,
                    Payment.APP_CARD,
                    "NOT_EXIST",
                    List.of(
                            new ReservedSeatRequest.SeatInfo("G", 4),
                            new ReservedSeatRequest.SeatInfo("G", 5)
                    )
            );
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_COUPON_CODE);
    }

    @Test
    @DisplayName("좌석 저장 중 중복 좌석이 감지되면 SEAT_ALREADY_RESERVED 예외가 발생한다")
    void createReservation_Fail_AlreadyReservedSeat() {
        // Given
        Long userId = 1L;
        Long screeningId = 1L;
        User user = User.builder().id(userId).nickname("우혁").build();
        Screening screening = createScreening(screeningId, TheaterType.NORMAL);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(screeningRepository.findByIdForUpdate(screeningId)).willReturn(Optional.of(screening));
        given(reservationRepository.save(any(Reservation.class))).willAnswer(i -> i.getArgument(0));
        given(reservedSeatRepository.saveAll(anyList())).willThrow(DataIntegrityViolationException.class);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            reservationService.createReservation(
                    userId,
                    screeningId,
                    2,
                    Payment.APP_CARD,
                    null,
                    List.of(
                            new ReservedSeatRequest.SeatInfo("G", 4),
                            new ReservedSeatRequest.SeatInfo("G", 5)
                    )
            );
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SEAT_ALREADY_RESERVED);
    }

    @Test
    @DisplayName("존재하지 않는 상영일정으로 예매 시 SCREENING_NOT_FOUND 예외가 발생한다")
    void createReservation_Fail_ScreeningNotFound() {
        // Given
        Long userId = 1L;
        Long invalidScreeningId = 999L;

        User user = User.builder().id(userId).nickname("우혁").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // 상영 일정이 없다고 가정
        given(screeningRepository.findByIdForUpdate(invalidScreeningId)).willReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            reservationService.createReservation(
                    userId,
                    invalidScreeningId,
                    2,
                    Payment.APP_CARD,
                    null,
                    List.of(
                            new ReservedSeatRequest.SeatInfo("G", 4),
                            new ReservedSeatRequest.SeatInfo("G", 5)
                    )
            );
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SCREENING_NOT_FOUND);
    }

    private Screening createScreening(Long screeningId, TheaterType theaterType) {
        Movie movie = Movie.builder().id(1L).title("테스트 영화").build();
        Theater theater = Theater.builder().id(1L).name("1관").type(theaterType).build();

        return Screening.builder()
                .id(screeningId)
                .movie(movie)
                .theater(theater)
                .isMorning(false)
                .build();
    }
}
