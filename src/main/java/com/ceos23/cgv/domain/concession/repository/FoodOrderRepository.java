package com.ceos23.cgv.domain.concession.repository;

import com.ceos23.cgv.domain.concession.entity.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
}