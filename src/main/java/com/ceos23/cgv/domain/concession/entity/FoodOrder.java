package com.ceos23.cgv.domain.concession.entity;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.concession.enums.FoodOrderStatus;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.global.entity.BaseTimeEntity;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "food_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FoodOrder extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FoodOrderStatus status;

    @Column(nullable = false, unique = true)
    private String paymentId;

    public static FoodOrder create(User user, Cinema cinema, String paymentId) {
        return FoodOrder.builder()
                .user(user)
                .cinema(cinema)
                .totalPrice(0)
                .status(FoodOrderStatus.PENDING)
                .paymentId(paymentId)
                .build();
    }

    // 총 결제 금액을 업데이트하는 메서드
    public void updateTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void calculateTotalPrice(List<OrderItem> orderItems) {
        this.totalPrice = orderItems.stream()
                .mapToInt(OrderItem::calculatePrice)
                .sum();
    }

    public void completePayment() {
        validatePending();
        this.status = FoodOrderStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status == FoodOrderStatus.CANCELED) {
            return;
        }
        this.status = FoodOrderStatus.CANCELED;
    }

    public void validatePending() {
        if (this.status != FoodOrderStatus.PENDING) {
            throw new CustomException(ErrorCode.FOOD_ORDER_NOT_PENDING);
        }
    }
}
