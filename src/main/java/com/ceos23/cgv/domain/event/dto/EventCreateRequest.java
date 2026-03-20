package com.ceos23.cgv.domain.event.dto;

import java.time.LocalDateTime;

public record EventCreateRequest(
        String title,
        String content,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}