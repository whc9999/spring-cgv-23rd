package com.ceos23.cgv.domain.user.dto;

import com.ceos23.cgv.domain.user.entity.Cinetalk;
import java.time.LocalDateTime;

public record CinetalkResponse(
        Long cinetalkId,
        String authorName,
        String content,
        int likeCount,
        LocalDateTime createdAt
) {
    public static CinetalkResponse from(Cinetalk cinetalk) {
        return new CinetalkResponse(
                cinetalk.getId(),
                cinetalk.getUser().getNickname(),
                cinetalk.getContent(),
                cinetalk.getLikeCount(),
                cinetalk.getCreatedAt()
        );
    }
}