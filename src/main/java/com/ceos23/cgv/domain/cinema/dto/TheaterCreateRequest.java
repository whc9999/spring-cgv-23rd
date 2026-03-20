package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.enums.TheaterType;

public record TheaterCreateRequest(
        Long cinemaId,
        String name,
        TheaterType type,
        String maxRow,
        int maxCol
) {
}