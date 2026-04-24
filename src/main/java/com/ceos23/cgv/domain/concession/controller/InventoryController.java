package com.ceos23.cgv.domain.concession.controller;

import com.ceos23.cgv.domain.concession.dto.InventoryResponse;
import com.ceos23.cgv.domain.concession.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "지점별 매점 상품 재고 관리 API")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/cinema/{cinemaId}")
    @Operation(summary = "지점별 재고 조회", description = "극장 ID를 통해 해당 지점의 모든 상품 재고 목록을 조회합니다.")
    public ResponseEntity<List<InventoryResponse>> getInventoriesByCinema(@PathVariable Long cinemaId) {
        List<InventoryResponse> responses = inventoryService.getInventoriesByCinemaId(cinemaId).stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
