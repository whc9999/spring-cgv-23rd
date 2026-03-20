package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.enums.TheaterType;

public record ReviewCreateRequest(
        Long userId,
        Long movieId,
        TheaterType type,
        String content
) {
}