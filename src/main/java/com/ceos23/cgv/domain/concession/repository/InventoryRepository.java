package com.ceos23.cgv.domain.concession.repository;

import com.ceos23.cgv.domain.concession.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}