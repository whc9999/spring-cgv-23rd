package com.ceos23.cgv.domain.movie.dto;

import com.ceos23.cgv.domain.movie.entity.MovieLike;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovieLikeResponse {
    private Long likeId;
    private Long movieId;
    private String movieTitle;

    public static MovieLikeResponse from(MovieLike movieLike) {
        return MovieLikeResponse.builder()
                .likeId(movieLike.getId())
                .movieId(movieLike.getMovie().getId())
                .movieTitle(movieLike.getMovie().getTitle())
                .build();
    }
}