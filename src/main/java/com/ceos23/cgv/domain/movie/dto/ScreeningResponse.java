package com.ceos23.cgv.domain.movie.dto;

import com.ceos23.cgv.domain.movie.entity.Screening;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ScreeningResponse {
    private Long screeningId;
    private String movieTitle;
    private String cinemaName;
    private String theaterName;
    private LocalDate startTime;
    private LocalDate endTime;
    private Boolean isMorning;

    public static ScreeningResponse from(Screening screening) {
        return ScreeningResponse.builder()
                .screeningId(screening.getId())
                .movieTitle(screening.getMovie().getTitle())
                .cinemaName(screening.getTheater().getCinema().getName())
                .theaterName(screening.getTheater().getName())
                .startTime(screening.getStartTime())
                .endTime(screening.getEndTime())
                .isMorning(screening.getIsMorning())
                .build();
    }
}