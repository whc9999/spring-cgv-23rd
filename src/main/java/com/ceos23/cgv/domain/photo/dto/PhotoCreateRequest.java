package com.ceos23.cgv.domain.photo.dto;

public record PhotoCreateRequest(
        Long movieId,
        Long personId,
        String name
) {
}