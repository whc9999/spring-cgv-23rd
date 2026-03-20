package com.ceos23.cgv.domain.movie.dto;

import java.time.LocalDate;

public record ScreeningCreateRequest(
        Long movieId,
        Long theaterId,
        LocalDate startTime,
        LocalDate endTime,
        Boolean isMorning
) {
}