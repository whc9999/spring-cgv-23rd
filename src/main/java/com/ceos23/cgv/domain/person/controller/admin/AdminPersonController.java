package com.ceos23.cgv.domain.person.controller.admin;

import com.ceos23.cgv.domain.person.dto.PersonCreateRequest;
import com.ceos23.cgv.domain.person.dto.WorkParticipationRequest;
import com.ceos23.cgv.domain.person.dto.WorkParticipationResponse;
import com.ceos23.cgv.domain.person.entity.Person;
import com.ceos23.cgv.domain.person.entity.WorkParticipation;
import com.ceos23.cgv.domain.person.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/persons")
@RequiredArgsConstructor
@Tag(name = "Admin Person API", description = "관리자 전용 영화 인물 및 참여작 관리 API")
public class AdminPersonController {

    private final PersonService personService;

    @PostMapping
    @Operation(summary = "인물 등록", description = "새로운 배우, 감독 등의 인물 정보를 DB에 등록합니다.")
    public ResponseEntity<Person> createPerson(@RequestBody PersonCreateRequest request) {
        Person person = personService.createPerson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(person);
    }

    @PostMapping("/participations")
    @Operation(summary = "영화 참여 정보 등록", description = "특정 인물을 특정 영화의 주연, 조연, 감독 등으로 연결합니다.")
    public ResponseEntity<WorkParticipationResponse> addParticipation(@RequestBody WorkParticipationRequest request) {
        WorkParticipation participation = personService.addWorkParticipation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WorkParticipationResponse.from(participation));
    }
}
