package com.ceos23.cgv.domain.event.dto;

import com.ceos23.cgv.domain.event.entity.Event;
import java.time.LocalDateTime;

public record EventResponse(
        Long eventId,
        String title,
        String content,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getContent(),
                event.getStartDate(),
                event.getEndDate()
        );
    }
}