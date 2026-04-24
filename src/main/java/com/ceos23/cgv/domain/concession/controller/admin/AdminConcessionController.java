package com.ceos23.cgv.domain.concession.controller.admin;

import com.ceos23.cgv.domain.concession.dto.ProductCreateRequest;
import com.ceos23.cgv.domain.concession.dto.ProductResponse;
import com.ceos23.cgv.domain.concession.entity.Product;
import com.ceos23.cgv.domain.concession.service.ConcessionService;
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
@RequestMapping("/api/admin/concessions")
@RequiredArgsConstructor
@Tag(name = "Admin Concession API", description = "관리자 전용 매점 상품 관리 API")
public class AdminConcessionController {

    private final ConcessionService concessionService;

    @PostMapping("/products")
    @Operation(summary = "매점 상품 등록", description = "새로운 매점 상품(팝콘, 콤보 등)을 DB에 등록합니다.")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductCreateRequest request) {
        Product createdProduct = concessionService.createProduct(
                request.name(),
                request.price(),
                request.description(),
                request.origin(),
                request.ingredient(),
                request.pickupPossible(),
                request.category()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(createdProduct));
    }
}
