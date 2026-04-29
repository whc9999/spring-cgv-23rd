package com.ceos23.cgv.domain.concession.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Min(value = 1, message = "최소 1개 이상 구매해야합니다.")
    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private FoodOrder foodOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public static OrderItem create(FoodOrder foodOrder, Product product, int quantity) {
        return OrderItem.builder()
                .foodOrder(foodOrder)
                .product(product)
                .quantity(quantity)
                .build();
    }

    public int calculatePrice() {
        return product.getPrice() * quantity;
    }
}
