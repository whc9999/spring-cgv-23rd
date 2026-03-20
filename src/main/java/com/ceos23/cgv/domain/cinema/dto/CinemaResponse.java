package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.entity.Cinema;

public record CinemaResponse(
        Long id,
        String name,
        String region
) {
    public static CinemaResponse from(Cinema cinema) {
        return new CinemaResponse(
                cinema.getId(),
                cinema.getName(),
                cinema.getRegion()
        );
    }
}