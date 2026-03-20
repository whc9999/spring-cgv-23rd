package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.entity.CinemaLike;

public record CinemaLikeResponse(
        Long likeId,
        Long cinemaId,
        String cinemaName
) {
    public static CinemaLikeResponse from(CinemaLike cinemaLike) {
        return new CinemaLikeResponse(
                cinemaLike.getId(),
                cinemaLike.getCinema().getId(),
                cinemaLike.getCinema().getName()
        );
    }
}