package com.ceos23.cgv.domain.user.dto;

public record CinetalkCreateRequest(
        Long userId,
        Long movieId,
        Long cinemaId,
        String content
) {
}