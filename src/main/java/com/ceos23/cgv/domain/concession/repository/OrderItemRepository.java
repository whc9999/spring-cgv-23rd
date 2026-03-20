package com.ceos23.cgv.domain.concession.repository;

import com.ceos23.cgv.domain.concession.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}