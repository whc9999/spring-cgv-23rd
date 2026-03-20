package com.ceos23.cgv.global.exception;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.List;
import java.util.stream.Collectors;

public record ErrorResponse(
        int status,
        String code,
        String message,
        List<ValidationError> errors
) {
    // 1. 일반적인 CustomException 응답용
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of() // 에러 리스트는 빈 배열로 반환
        );
    }

    // 2. @Valid 유효성 검사 실패(@RequestBody) 응답용
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                ValidationError.of(bindingResult)
        );
    }

    // Validation 상세 에러를 담기 위한 내부 record
    public record ValidationError(String field, String value, String reason) {
        private static List<ValidationError> of(final BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> new ValidationError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }
}