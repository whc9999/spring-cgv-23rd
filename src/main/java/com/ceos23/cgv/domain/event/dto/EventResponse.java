package com.ceos23.cgv.domain.event.dto;

import com.ceos23.cgv.domain.event.entity.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {
    private Long eventId;
    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .content(event.getContent())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .build();
    }
}