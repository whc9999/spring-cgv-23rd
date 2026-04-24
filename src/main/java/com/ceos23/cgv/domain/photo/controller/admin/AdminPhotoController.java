package com.ceos23.cgv.domain.photo.controller.admin;

import com.ceos23.cgv.domain.photo.dto.PhotoCreateRequest;
import com.ceos23.cgv.domain.photo.dto.PhotoResponse;
import com.ceos23.cgv.domain.photo.entity.Photo;
import com.ceos23.cgv.domain.photo.service.PhotoService;
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
@RequestMapping("/api/admin/photos")
@RequiredArgsConstructor
@Tag(name = "Admin Photo API", description = "관리자 전용 영화 및 인물 사진 관리 API")
public class AdminPhotoController {

    private final PhotoService photoService;

    @PostMapping
    @Operation(summary = "사진 등록", description = "영화 또는 인물에 연관된 사진 URL(name)을 등록합니다. (movieId나 personId 중 하나 필수)")
    public ResponseEntity<PhotoResponse> createPhoto(@RequestBody PhotoCreateRequest request) {
        Photo photo = photoService.createPhoto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PhotoResponse.from(photo));
    }
}
