package com.ceos23.cgv.domain.concession.service;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.repository.CinemaRepository;
import com.ceos23.cgv.domain.concession.dto.FoodOrderRequest;
import com.ceos23.cgv.domain.concession.entity.FoodOrder;
import com.ceos23.cgv.domain.concession.entity.OrderItem;
import com.ceos23.cgv.domain.concession.entity.Product;
import com.ceos23.cgv.domain.concession.enums.ProductCategory;
import com.ceos23.cgv.domain.concession.repository.FoodOrderRepository;
import com.ceos23.cgv.domain.concession.repository.OrderItemRepository;
import com.ceos23.cgv.domain.concession.repository.ProductRepository;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcessionService {

    private final ProductRepository productRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CinemaRepository cinemaRepository;

    /**
     * [GET] 매점의 모든 상품 목록 조회
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * [POST] 매점 상품 주문하기 (복합 로직)
     */
    @Transactional
    public FoodOrder createOrder(FoodOrderRequest request) {
        // 1. 유저와 픽업할 영화관 지점 조회
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Cinema cinema = cinemaRepository.findById(request.cinemaId())
                .orElseThrow(() -> new CustomException(ErrorCode.CINEMA_NOT_FOUND));

        int calculatedTotalPrice = 0;

        FoodOrder foodOrder = FoodOrder.builder()
                .user(user)
                .cinema(cinema)
                .totalPrice(0)
                .build();
        foodOrderRepository.save(foodOrder);

        for (FoodOrderRequest.OrderItemRequest itemReq : request.orderItems()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            calculatedTotalPrice += (product.getPrice() * itemReq.quantity());

            OrderItem orderItem = OrderItem.builder()
                    .foodOrder(foodOrder)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .build();
            orderItemRepository.save(orderItem);
        }

        FoodOrder finalOrder = FoodOrder.builder()
                .id(foodOrder.getId())
                .user(user)
                .cinema(cinema)
                .totalPrice(calculatedTotalPrice)
                .build();

        return foodOrderRepository.save(finalOrder);
    }

    /**
     * [POST] 새로운 매점 상품 등록 (관리자용)
     */
    @Transactional
    public Product createProduct(String name, int price, String description,
                                 String origin, String ingredient,
                                 Boolean pickupPossible, ProductCategory category) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .description(description)
                .origin(origin)
                .ingredient(ingredient)
                .pickupPossible(pickupPossible)
                .category(category)
                .build();

        return productRepository.save(product);
    }

    /**
     * [GET] 특정 유저의 매점 주문 내역 조회
     */
    public List<FoodOrder> getOrdersByUserId(Long userId) {
        return foodOrderRepository.findByUserId(userId);
    }
}