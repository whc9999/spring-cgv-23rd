package com.ceos23.cgv.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (공통 에러)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 에러가 발생했습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "C004", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

    // Domain: User & Cinetalk
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 유저입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 가입되어 있는 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "U003", "이미 사용 중인 닉네임입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "U004", "유효하지 않거나 만료된 Refresh Token 입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "U005", "토큰 정보가 일치하지 않습니다. (탈취 의심)"),

    // Domain: Movie & Screening
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 영화입니다."),
    SCREENING_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "상영 일정을 찾을 수 없습니다."),

    // Domain: Cinema & Theater
    CINEMA_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "존재하지 않는 극장 지점입니다."),
    THEATER_NOT_FOUND(HttpStatus.NOT_FOUND, "T002", "존재하지 않는 상영관입니다."),

    // Domain: Reservation
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "예매 정보를 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "R002", "이미 예매가 완료된 좌석입니다. 다른 좌석을 선택해 주세요."),
    RESERVATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "R003", "본인의 예매 내역만 취소할 수 있습니다."),
    RESERVATION_ALREADY_CANCELED(HttpStatus.CONFLICT, "R004", "이미 취소 처리된 예매입니다."),
    INVALID_COUPON_CODE(HttpStatus.BAD_REQUEST, "R005", "유효하지 않은 쿠폰 코드입니다."),
    PAYMENT_FAILED(HttpStatus.BAD_GATEWAY, "R006", "결제 처리에 실패했습니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "R007", "결제 취소에 실패했습니다."),
    PAYMENT_NOT_COMPLETED(HttpStatus.CONFLICT, "R008", "결제 완료 상태의 예매만 취소할 수 있습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "R009", "결제 정보를 찾을 수 없습니다."),

    // Domain: Concession & Inventory
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 매점 상품입니다."),
    INVENTORY_SHORTAGE(HttpStatus.BAD_REQUEST, "S002", "상품의 재고가 부족합니다."),
    INVALID_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "S003", "재고는 최소 1개 이상이어야 합니다."),
    FOOD_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "S004", "매점 주문 정보를 찾을 수 없습니다."),
    FOOD_ORDER_NOT_PENDING(HttpStatus.CONFLICT, "S005", "결제 대기 상태의 매점 주문만 완료할 수 있습니다."),

    // Domain: Person, Event, Photo
    PERSON_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "인물을 찾을 수 없습니다."),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다."),
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "PH001", "사진을 찾을 수 없습니다."),
    PHOTO_TARGET_REQUIRED(HttpStatus.BAD_REQUEST, "PH002", "사진은 최소한 영화(movieId) 또는 인물(personId) 중 하나 이상의 대상에 등록되어야 합니다."),

    // Domain: Region
    UNSUPPORTED_REGION(HttpStatus.BAD_REQUEST, "RG001", "지원하지 않는 지역 이름입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    public String getDescription() {
        return message;
    }
}
