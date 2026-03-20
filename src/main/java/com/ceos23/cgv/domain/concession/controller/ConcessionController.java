package com.ceos23.cgv.domain.concession.controller;

import com.ceos23.cgv.domain.concession.dto.FoodOrderRequest;
import com.ceos23.cgv.domain.concession.dto.FoodOrderResponse;
import com.ceos23.cgv.domain.concession.dto.ProductCreateRequest;
import com.ceos23.cgv.domain.concession.dto.ProductResponse;
import com.ceos23.cgv.domain.concession.entity.FoodOrder;
import com.ceos23.cgv.domain.concession.entity.Product;
import com.ceos23.cgv.domain.concession.service.ConcessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/concessions")
@RequiredArgsConstructor
@Tag(name = "Concession API", description = "매점 상품 조회 및 픽업 주문 API")
public class ConcessionController {

    private final ConcessionService concessionService;

    @GetMapping("/products")
    @Operation(summary = "매점 상품 전체 조회", description = "팝콘, 음료 등 매점에서 판매하는 전체 상품 목록을 가져옵니다.")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> responses = concessionService.getAllProducts().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/orders")
    @Operation(summary = "매점 패스트오더 주문", description = "유저, 픽업 영화관, 그리고 장바구니에 담긴 상품 목록들을 받아 총액을 계산하고 주문을 생성합니다.")
    public ResponseEntity<FoodOrderResponse> createOrder(@RequestBody FoodOrderRequest request) {
        FoodOrder order = concessionService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FoodOrderResponse.from(order));
    }

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

    @GetMapping("/orders")
    @Operation(summary = "유저별 매점 주문 내역 조회", description = "특정 유저(userId)가 주문한 매점 결제 내역 목록을 조회합니다.")
    public ResponseEntity<List<FoodOrderResponse>> getOrdersByUserId(@RequestParam Long userId) {
        // 임시로 @RequestParam을 통해 userId를 쿼리 파라미터로 받습니다. (예: /api/concessions/orders?userId=1)
        List<FoodOrderResponse> responses = concessionService.getOrdersByUserId(userId).stream()
                .map(FoodOrderResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}