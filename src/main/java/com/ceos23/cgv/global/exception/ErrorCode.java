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

    // Domain: User & Cinetalk
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 유저입니다."),

    // Domain: Movie & Screening
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 영화입니다."),
    SCREENING_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "상영 일정을 찾을 수 없습니다."),

    // Domain: Cinema & Theater
    CINEMA_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "존재하지 않는 극장 지점입니다."),
    THEATER_NOT_FOUND(HttpStatus.NOT_FOUND, "T002", "존재하지 않는 상영관입니다."),

    // Domain: Reservation
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "예매 정보를 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "R002", "이미 예매가 완료된 좌석입니다. 다른 좌석을 선택해 주세요."),

    // Domain: Concession & Inventory
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 매점 상품입니다."),
    INVENTORY_SHORTAGE(HttpStatus.BAD_REQUEST, "S002", "상품의 재고가 부족하거나 최소 1개 이상이어야 합니다."),

    // Domain: Person, Event, Photo
    PERSON_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "인물을 찾을 수 없습니다."),
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다."),
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "PH001", "사진을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}