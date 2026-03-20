package com.ceos23.cgv.domain.photo.dto;

import com.ceos23.cgv.domain.photo.entity.Photo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoResponse {
    private Long photoId;
    private String name;
    private Long movieId;
    private Long personId;

    public static PhotoResponse from(Photo photo) {
        return PhotoResponse.builder()
                .photoId(photo.getId())
                .name(photo.getName())
                // NullPointerException 방지를 위해 삼항 연산자 사용
                .movieId(photo.getMovie() != null ? photo.getMovie().getId() : null)
                .personId(photo.getPerson() != null ? photo.getPerson().getId() : null)
                .build();
    }
}