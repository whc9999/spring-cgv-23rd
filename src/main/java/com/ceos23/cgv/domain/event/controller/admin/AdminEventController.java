package com.ceos23.cgv.domain.event.controller.admin;

import com.ceos23.cgv.domain.event.dto.EventCreateRequest;
import com.ceos23.cgv.domain.event.dto.EventResponse;
import com.ceos23.cgv.domain.event.entity.Event;
import com.ceos23.cgv.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@Tag(name = "Admin Event API", description = "관리자 전용 이벤트 관리 API")
public class AdminEventController {

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
}
