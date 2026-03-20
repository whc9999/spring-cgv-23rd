package com.ceos23.cgv.domain.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ScreeningCreateRequest {
    private Long movieId;
    private Long theaterId;
    private LocalDate startTime;
    private LocalDate endTime;
    private Boolean isMorning;
}