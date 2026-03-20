package com.ceos23.cgv.domain.photo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhotoCreateRequest {
    // 영화 사진일 경우 값을 넣고, 인물 사진이면 null
    private Long movieId;

    // 인물 사진일 경우 값을 넣고, 영화 사진이면 null
    private Long personId;

    private String name;    // 사진 파일명 또는 URL
}