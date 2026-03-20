package com.ceos23.cgv.domain.event.controller;

import com.ceos23.cgv.domain.event.dto.EventCreateRequest;
import com.ceos23.cgv.domain.event.dto.EventResponse;
import com.ceos23.cgv.domain.event.entity.Event;
import com.ceos23.cgv.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    @Operation(summary = "이벤트 생성", description = "새로운 이벤트를 등록합니다.")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventCreateRequest request) {
        Event event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }

    @PostMapping("/{eventId}/movies/{movieId}")
    @Operation(summary = "이벤트-영화 연결", description = "생성된 이벤트를 특정 영화와 연결(매핑)합니다.")
    public ResponseEntity<String> linkEventToMovie(@PathVariable Long eventId, @PathVariable Long movieId) {
        eventService.linkEventToMovie(eventId, movieId);
        return ResponseEntity.status(HttpStatus.CREATED).body("이벤트와 영화가 성공적으로 연결되었습니다.");
    }

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