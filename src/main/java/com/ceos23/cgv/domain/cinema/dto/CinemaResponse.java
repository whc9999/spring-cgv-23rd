package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CinemaResponse {
    private Long id;
    private String name;
    private String region;
    private boolean isSpecial;

    public static CinemaResponse from(Cinema cinema) {
        return CinemaResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .region(cinema.getRegion())
                .isSpecial(cinema.isSpecial())
                .build();
    }
}