package com.ceos23.cgv.domain.movie.dto;

import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.enums.Genre;
import com.ceos23.cgv.domain.movie.enums.MovieRating;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MovieResponse {
    private Long id;
    private String title;
    private int runningTime;
    private Double salesRate;
    private LocalDate releaseDate;
    private MovieRating movieRating;
    private Genre genre;
    private String prologue;

    public static MovieResponse from(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .runningTime(movie.getRunningTime())
                .salesRate(movie.getSalesRate())
                .releaseDate(movie.getReleaseDate())
                .movieRating(movie.getMovieRating())
                .genre(movie.getGenre())
                .prologue(movie.getPrologue())
                .build();
    }
}