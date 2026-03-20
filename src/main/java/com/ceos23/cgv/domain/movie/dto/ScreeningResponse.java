package com.ceos23.cgv.domain.movie.dto;

import com.ceos23.cgv.domain.movie.entity.Screening;
import java.time.LocalDate;

public record ScreeningResponse(
        Long screeningId,
        String movieTitle,
        String cinemaName,
        String theaterName,
        LocalDate startTime,
        LocalDate endTime,
        Boolean isMorning
) {
    public static ScreeningResponse from(Screening screening) {
        return new ScreeningResponse(
                screening.getId(),
                screening.getMovie().getTitle(),
                screening.getTheater().getCinema().getName(),
                screening.getTheater().getName(),
                screening.getStartTime(),
                screening.getEndTime(),
                screening.getIsMorning()
        );
    }
}