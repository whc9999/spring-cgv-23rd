package com.ceos23.cgv.domain.photo.controller;

import com.ceos23.cgv.domain.photo.dto.PhotoCreateRequest;
import com.ceos23.cgv.domain.photo.dto.PhotoResponse;
import com.ceos23.cgv.domain.photo.entity.Photo;
import com.ceos23.cgv.domain.photo.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Tag(name = "Photo API", description = "영화 및 인물 사진 등록 및 조회 API")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping
    @Operation(summary = "사진 등록", description = "영화 또는 인물에 연관된 사진 URL(name)을 등록합니다. (movieId나 personId 중 하나 필수)")
    public ResponseEntity<PhotoResponse> createPhoto(@RequestBody PhotoCreateRequest request) {
        Photo photo = photoService.createPhoto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PhotoResponse.from(photo));
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "특정 영화의 사진 조회", description = "영화 ID를 통해 해당 영화의 스틸컷/포스터 목록을 불러옵니다.")
    public ResponseEntity<List<PhotoResponse>> getPhotosByMovie(@PathVariable Long movieId) {
        List<PhotoResponse> responses = photoService.getPhotosByMovieId(movieId).stream()
                .map(PhotoResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/person/{personId}")
    @Operation(summary = "특정 인물의 사진 조회", description = "인물 ID를 통해 해당 배우/감독의 사진 목록을 불러옵니다.")
    public ResponseEntity<List<PhotoResponse>> getPhotosByPerson(@PathVariable Long personId) {
        List<PhotoResponse> responses = photoService.getPhotosByPersonId(personId).stream()
                .map(PhotoResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}