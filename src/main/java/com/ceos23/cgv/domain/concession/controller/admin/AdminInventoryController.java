package com.ceos23.cgv.domain.concession.controller.admin;

import com.ceos23.cgv.domain.concession.dto.InventoryResponse;
import com.ceos23.cgv.domain.concession.dto.InventoryUpdateRequest;
import com.ceos23.cgv.domain.concession.entity.Inventory;
import com.ceos23.cgv.domain.concession.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventories")
@RequiredArgsConstructor
@Tag(name = "Admin Inventory API", description = "관리자 전용 지점별 매점 상품 재고 관리 API")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/update")
    @Operation(summary = "재고 업데이트", description = "특정 지점의 상품 재고를 추가하거나 차감합니다. 재고는 최소 1개 이상이어야 합니다.")
    public ResponseEntity<InventoryResponse> updateInventory(@RequestBody InventoryUpdateRequest request) {
        Inventory inventory = inventoryService.updateInventory(request);
        return ResponseEntity.ok(InventoryResponse.from(inventory));
    }
}
