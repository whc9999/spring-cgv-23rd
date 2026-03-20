package com.ceos23.cgv.domain.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EventCreateRequest {
    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}