package com.ceos23.cgv.domain.movie.dto;

import com.ceos23.cgv.domain.movie.enums.Genre;
import com.ceos23.cgv.domain.movie.enums.MovieRating;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MovieCreateRequest {
    private String title;
    private int runningTime;
    private LocalDate releaseDate;
    private MovieRating movieRating;
    private Genre genre;
    private String prologue;
}