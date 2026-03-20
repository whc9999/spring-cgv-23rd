package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.entity.CinemaLike;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CinemaLikeResponse {
    private Long likeId;
    private Long cinemaId;
    private String cinemaName;

    public static CinemaLikeResponse from(CinemaLike cinemaLike) {
        return CinemaLikeResponse.builder()
                .likeId(cinemaLike.getId())
                .cinemaId(cinemaLike.getCinema().getId())
                .cinemaName(cinemaLike.getCinema().getName())
                .build();
    }
}