package com.ceos23.cgv.domain.photo.dto;

import com.ceos23.cgv.domain.photo.entity.Photo;

public record PhotoResponse(
        Long photoId,
        String name,
        Long movieId,
        Long personId
) {
    public static PhotoResponse from(Photo photo) {
        return new PhotoResponse(
                photo.getId(),
                photo.getName(),
                photo.getMovie() != null ? photo.getMovie().getId() : null,
                photo.getPerson() != null ? photo.getPerson().getId() : null
        );
    }
}