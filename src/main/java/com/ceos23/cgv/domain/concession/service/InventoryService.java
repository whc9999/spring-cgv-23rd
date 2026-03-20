package com.ceos23.cgv.domain.concession.service;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.repository.CinemaRepository;
import com.ceos23.cgv.domain.concession.dto.InventoryUpdateRequest;
import com.ceos23.cgv.domain.concession.entity.Inventory;
import com.ceos23.cgv.domain.concession.entity.Product;
import com.ceos23.cgv.domain.concession.repository.InventoryRepository;
import com.ceos23.cgv.domain.concession.repository.ProductRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final CinemaRepository cinemaRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Inventory updateInventory(InventoryUpdateRequest request) {
        Cinema cinema = cinemaRepository.findById(request.cinemaId())
                .orElseThrow(() -> new CustomException(ErrorCode.CINEMA_NOT_FOUND));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        Optional<Inventory> existingInventory = inventoryRepository.findByCinemaIdAndProductId(
                request.cinemaId(), request.productId()
        );

        if (existingInventory.isPresent()) {
            Inventory inventory = existingInventory.get();
            int newStock = inventory.getStockQuantity() + request.quantity();

            if (newStock < 1) {
                throw new CustomException(ErrorCode.INVENTORY_SHORTAGE);
            }

            Inventory updatedInventory = Inventory.builder()
                    .id(inventory.getId())
                    .cinema(cinema)
                    .product(product)
                    .stockQuantity(newStock)
                    .build();
            return inventoryRepository.save(updatedInventory);
        } else {
            // 처음 입고되는 상품일 경우
            if (request.quantity() < 1) {
                throw new CustomException(ErrorCode.INVENTORY_SHORTAGE);
            }
            Inventory newInventory = Inventory.builder()
                    .cinema(cinema)
                    .product(product)
                    .stockQuantity(request.quantity())
                    .build();
            return inventoryRepository.save(newInventory);
        }
    }

    public List<Inventory> getInventoriesByCinemaId(Long cinemaId) {
        return inventoryRepository.findByCinemaId(cinemaId);
    }
}