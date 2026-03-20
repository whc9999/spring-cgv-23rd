package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.entity.Theater;
import com.ceos23.cgv.domain.cinema.enums.TheaterType;

public record TheaterResponse(
        Long id,
        String name,
        TheaterType type,
        String maxRow,
        int maxCol
) {
    public static TheaterResponse from(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getType(),
                theater.getMaxRow(),
                theater.getMaxCol()
        );
    }
}