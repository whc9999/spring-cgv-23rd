package com.ceos23.cgv.domain.event.controller;

import com.ceos23.cgv.domain.event.dto.EventResponse;
import com.ceos23.cgv.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "영화 관련 이벤트(기획전, 무대인사 등) 등록 및 조회 API")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "전체 이벤트 조회", description = "현재 등록된 모든 이벤트 목록을 가져옵니다.")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> responses = eventService.getAllEvents().stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "특정 영화의 이벤트 조회", description = "영화 ID를 통해 해당 영화와 관련된 이벤트 목록만 가져옵니다.")
    public ResponseEntity<List<EventResponse>> getEventsByMovie(@PathVariable Long movieId) {
        List<EventResponse> responses = eventService.getEventsByMovieId(movieId).stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
