package com.ceos23.cgv.domain.cinema.dto;

import com.ceos23.cgv.domain.cinema.entity.Review;
import com.ceos23.cgv.domain.cinema.enums.TheaterType;
import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        String authorName,
        String movieTitle,
        TheaterType theaterType,
        String content,
        int likeCount,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getNickname(),
                review.getMovie().getTitle(),
                review.getType(),
                review.getContent(),
                review.getLikeCount(),
                review.getCreatedAt()
        );
    }
}