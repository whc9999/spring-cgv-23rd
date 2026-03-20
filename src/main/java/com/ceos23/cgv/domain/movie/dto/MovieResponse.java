package com.ceos23.cgv.domain.movie.dto;

import com.ceos23.cgv.domain.movie.entity.Movie;
import com.ceos23.cgv.domain.movie.enums.Genre;
import com.ceos23.cgv.domain.movie.enums.MovieRating;

import java.time.LocalDate;

public record MovieResponse(
        Long id,
        String title,
        int runningTime,
        Double salesRate,
        LocalDate releaseDate,
        MovieRating movieRating,
        Genre genre,
        String prologue
) {
    public static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getRunningTime(),
                movie.getSalesRate(),
                movie.getReleaseDate(),
                movie.getMovieRating(),
                movie.getGenre(),
                movie.getPrologue()
        );
    }
}