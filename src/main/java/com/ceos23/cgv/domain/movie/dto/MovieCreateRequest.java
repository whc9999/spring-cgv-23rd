package com.ceos23.cgv.domain.movie.dto;

import com.ceos23.cgv.domain.movie.enums.Genre;
import com.ceos23.cgv.domain.movie.enums.MovieRating;
import java.time.LocalDate;

public record MovieCreateRequest(
        String title,
        int runningTime,
        LocalDate releaseDate,
        MovieRating movieRating,
        Genre genre,
        String prologue
) {
}