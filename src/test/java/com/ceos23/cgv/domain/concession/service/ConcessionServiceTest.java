package com.ceos23.cgv.domain.concession.service;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.repository.CinemaRepository;
import com.ceos23.cgv.domain.concession.dto.FoodOrderRequest;
import com.ceos23.cgv.domain.concession.entity.FoodOrder;
import com.ceos23.cgv.domain.concession.entity.Inventory;
import com.ceos23.cgv.domain.concession.entity.Product;
import com.ceos23.cgv.domain.concession.enums.FoodOrderStatus;
import com.ceos23.cgv.domain.concession.repository.FoodOrderRepository;
import com.ceos23.cgv.domain.concession.repository.InventoryRepository;
import com.ceos23.cgv.domain.concession.repository.OrderItemRepository;
import com.ceos23.cgv.domain.concession.repository.ProductRepository;
import com.ceos23.cgv.domain.payment.service.PaymentService;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConcessionServiceTest {

    private static final String PAYMENT_ID = "food-order-payment-id";

    @Mock
    private FoodOrderRepository foodOrderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ConcessionService concessionService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());

        concessionService = new ConcessionService(
                productRepository,
                foodOrderRepository,
                orderItemRepository,
                userRepository,
                cinemaRepository,
                inventoryRepository,
                paymentService,
                transactionManager
        );
    }

    @Test
    @DisplayName("유저별 매점 주문 내역 조회 시 결제 완료 주문만 조회한다")
    void getOrdersByUserId_Success_OnlyCompletedOrders() {
        // Given
        User user = User.builder().id(1L).nickname("우혁").build();
        Cinema cinema = Cinema.builder().id(1L).name("CGV 신촌").build();
        FoodOrder completedOrder = FoodOrder.builder()
                .id(1L)
                .user(user)
                .cinema(cinema)
                .totalPrice(13000)
                .status(FoodOrderStatus.COMPLETED)
                .paymentId(PAYMENT_ID)
                .build();

        given(foodOrderRepository.findByUserIdAndStatusWithFetchJoin(1L, FoodOrderStatus.COMPLETED))
                .willReturn(List.of(completedOrder));

        // When
        List<FoodOrder> orders = concessionService.getOrdersByUserId(1L);

        // Then
        assertThat(orders).containsExactly(completedOrder);
        verify(foodOrderRepository).findByUserIdAndStatusWithFetchJoin(1L, FoodOrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("매점 주문 시 팝콘 2개와 콜라 1개의 총액이 정상적으로 계산되어 저장된다")
    void createOrder_Success_TotalPriceCalculated() {
        // Given (준비)
        User user = User.builder().id(1L).nickname("우혁").build();
        Cinema cinema = Cinema.builder().id(1L).name("CGV 신촌").build();

        // 팝콘(5,000원)과 콜라(3,000원) 엔티티 모킹
        Product popcorn = Product.builder().id(1L).name("달콤 팝콘").price(5000).build();
        Product cola = Product.builder().id(2L).name("콜라").price(3000).build();
        Inventory popcornInventory = Inventory.builder()
                .id(1L).cinema(cinema).product(popcorn).stockQuantity(10).build();
        Inventory colaInventory = Inventory.builder()
                .id(2L).cinema(cinema).product(cola).stockQuantity(10).build();

        // 팝콘 2개, 콜라 1개 주문 요청 (기대 총액 = 5000*2 + 3000*1 = 13000원)
        FoodOrderRequest request = new FoodOrderRequest(
                1L, 1L,
                List.of(
                        new FoodOrderRequest.OrderItemRequest(1L, 2), // 팝콘 2개
                        new FoodOrderRequest.OrderItemRequest(2L, 1)  // 콜라 1개
                )
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(cinemaRepository.findById(1L)).willReturn(Optional.of(cinema));
        given(productRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(popcorn, cola));
        given(paymentService.createFoodOrderPaymentId()).willReturn(PAYMENT_ID);
        given(inventoryRepository.findByCinemaIdAndProductIdForUpdate(1L, 1L)).willReturn(Optional.of(popcornInventory));
        given(inventoryRepository.findByCinemaIdAndProductIdForUpdate(1L, 2L)).willReturn(Optional.of(colaInventory));

        // save 메서드 호출 시 인자로 넘어온 엔티티를 그대로 반환하도록 처리
        AtomicReference<FoodOrder> savedOrderRef = new AtomicReference<>();
        given(foodOrderRepository.save(any(FoodOrder.class))).willAnswer(invocation -> {
            FoodOrder foodOrder = invocation.getArgument(0);
            savedOrderRef.set(foodOrder);
            return foodOrder;
        });
        given(foodOrderRepository.findByPaymentId(PAYMENT_ID)).willAnswer(invocation ->
                Optional.of(savedOrderRef.get()));
        given(orderItemRepository.findByFoodOrderId(any())).willAnswer(invocation ->
                List.of(
                        com.ceos23.cgv.domain.concession.entity.OrderItem.create(savedOrderRef.get(), popcorn, 2),
                        com.ceos23.cgv.domain.concession.entity.OrderItem.create(savedOrderRef.get(), cola, 1)
                ));

        // When (실행)
        FoodOrder savedOrder = concessionService.createOrder(request);

        // Then (검증)
        assertThat(savedOrder.getTotalPrice()).isEqualTo(13000);
        assertThat(savedOrder.getStatus()).isEqualTo(FoodOrderStatus.COMPLETED);
        assertThat(savedOrder.getUser().getNickname()).isEqualTo("우혁");
        assertThat(popcornInventory.getStockQuantity()).isEqualTo(8);
        assertThat(colaInventory.getStockQuantity()).isEqualTo(9);

        verify(foodOrderRepository).save(any(FoodOrder.class));
        verify(orderItemRepository).saveAll(anyList());
        verify(paymentService).requestInstantPayment(savedOrderRef.get());
    }

    @Test
    @DisplayName("매점 재고 차감 시 productId 오름차순으로 락을 획득한다")
    void createOrder_Success_DecreaseInventoryStocksInProductIdOrder() {
        // Given
        User user = User.builder().id(1L).nickname("우혁").build();
        Cinema cinema = Cinema.builder().id(1L).name("CGV 신촌").build();
        Product popcorn = Product.builder().id(1L).name("달콤 팝콘").price(5000).build();
        Product cola = Product.builder().id(2L).name("콜라").price(3000).build();
        Inventory popcornInventory = Inventory.builder()
                .id(1L).cinema(cinema).product(popcorn).stockQuantity(10).build();
        Inventory colaInventory = Inventory.builder()
                .id(2L).cinema(cinema).product(cola).stockQuantity(10).build();
        FoodOrderRequest request = new FoodOrderRequest(
                1L, 1L,
                List.of(
                        new FoodOrderRequest.OrderItemRequest(2L, 1),
                        new FoodOrderRequest.OrderItemRequest(1L, 2)
                )
        );

        AtomicReference<FoodOrder> savedOrderRef = new AtomicReference<>();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(cinemaRepository.findById(1L)).willReturn(Optional.of(cinema));
        given(productRepository.findAllById(List.of(2L, 1L))).willReturn(List.of(cola, popcorn));
        given(paymentService.createFoodOrderPaymentId()).willReturn(PAYMENT_ID);
        given(foodOrderRepository.save(any(FoodOrder.class))).willAnswer(invocation -> {
            FoodOrder foodOrder = invocation.getArgument(0);
            savedOrderRef.set(foodOrder);
            return foodOrder;
        });
        given(foodOrderRepository.findByPaymentId(PAYMENT_ID)).willAnswer(invocation ->
                Optional.of(savedOrderRef.get()));
        given(orderItemRepository.findByFoodOrderId(any())).willAnswer(invocation ->
                List.of(
                        com.ceos23.cgv.domain.concession.entity.OrderItem.create(savedOrderRef.get(), cola, 1),
                        com.ceos23.cgv.domain.concession.entity.OrderItem.create(savedOrderRef.get(), popcorn, 2)
                ));
        given(inventoryRepository.findByCinemaIdAndProductIdForUpdate(1L, 1L)).willReturn(Optional.of(popcornInventory));
        given(inventoryRepository.findByCinemaIdAndProductIdForUpdate(1L, 2L)).willReturn(Optional.of(colaInventory));

        // When
        concessionService.createOrder(request);

        // Then
        var inOrder = inOrder(inventoryRepository);
        inOrder.verify(inventoryRepository).findByCinemaIdAndProductIdForUpdate(1L, 1L);
        inOrder.verify(inventoryRepository).findByCinemaIdAndProductIdForUpdate(1L, 2L);
    }

    @Test
    @DisplayName("매점 결제 실패 시 PENDING 주문을 취소하고 재고는 차감하지 않는다")
    void createOrder_Fail_PaymentFailed() {
        // Given
        User user = User.builder().id(1L).nickname("우혁").build();
        Cinema cinema = Cinema.builder().id(1L).name("CGV 신촌").build();
        Product popcorn = Product.builder().id(1L).name("달콤 팝콘").price(5000).build();
        Inventory popcornInventory = Inventory.builder()
                .id(1L).cinema(cinema).product(popcorn).stockQuantity(10).build();
        FoodOrderRequest request = new FoodOrderRequest(
                1L, 1L,
                List.of(new FoodOrderRequest.OrderItemRequest(1L, 2))
        );

        AtomicReference<FoodOrder> savedOrderRef = new AtomicReference<>();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(cinemaRepository.findById(1L)).willReturn(Optional.of(cinema));
        given(productRepository.findAllById(List.of(1L))).willReturn(List.of(popcorn));
        given(paymentService.createFoodOrderPaymentId()).willReturn(PAYMENT_ID);
        given(foodOrderRepository.save(any(FoodOrder.class))).willAnswer(invocation -> {
            FoodOrder foodOrder = invocation.getArgument(0);
            savedOrderRef.set(foodOrder);
            return foodOrder;
        });
        given(foodOrderRepository.findByPaymentId(PAYMENT_ID)).willAnswer(invocation ->
                Optional.of(savedOrderRef.get()));
        given(paymentService.requestInstantPayment(any(FoodOrder.class)))
                .willThrow(new CustomException(ErrorCode.PAYMENT_FAILED));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                concessionService.createOrder(request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_FAILED);
        assertThat(savedOrderRef.get().getStatus()).isEqualTo(FoodOrderStatus.CANCELED);
        assertThat(popcornInventory.getStockQuantity()).isEqualTo(10);
        verify(inventoryRepository, never()).findByCinemaIdAndProductIdForUpdate(any(), any());
    }

    @Test
    @DisplayName("매점 결제 성공 후 재고가 부족하면 외부 결제를 보상 취소하고 주문을 취소한다")
    void createOrder_Fail_InventoryShortageAfterPayment() {
        // Given
        User user = User.builder().id(1L).nickname("우혁").build();
        Cinema cinema = Cinema.builder().id(1L).name("CGV 신촌").build();
        Product popcorn = Product.builder().id(1L).name("달콤 팝콘").price(5000).build();
        Inventory popcornInventory = Inventory.builder()
                .id(1L).cinema(cinema).product(popcorn).stockQuantity(1).build();
        FoodOrderRequest request = new FoodOrderRequest(
                1L, 1L,
                List.of(new FoodOrderRequest.OrderItemRequest(1L, 2))
        );

        AtomicReference<FoodOrder> savedOrderRef = new AtomicReference<>();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(cinemaRepository.findById(1L)).willReturn(Optional.of(cinema));
        given(productRepository.findAllById(List.of(1L))).willReturn(List.of(popcorn));
        given(paymentService.createFoodOrderPaymentId()).willReturn(PAYMENT_ID);
        given(foodOrderRepository.save(any(FoodOrder.class))).willAnswer(invocation -> {
            FoodOrder foodOrder = invocation.getArgument(0);
            savedOrderRef.set(foodOrder);
            return foodOrder;
        });
        given(foodOrderRepository.findByPaymentId(PAYMENT_ID)).willAnswer(invocation ->
                Optional.of(savedOrderRef.get()));
        given(orderItemRepository.findByFoodOrderId(any())).willAnswer(invocation ->
                List.of(com.ceos23.cgv.domain.concession.entity.OrderItem.create(savedOrderRef.get(), popcorn, 2)));
        given(inventoryRepository.findByCinemaIdAndProductIdForUpdate(1L, 1L)).willReturn(Optional.of(popcornInventory));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                concessionService.createOrder(request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVENTORY_SHORTAGE);
        assertThat(savedOrderRef.get().getStatus()).isEqualTo(FoodOrderStatus.CANCELED);
        assertThat(popcornInventory.getStockQuantity()).isEqualTo(1);
        verify(paymentService).cancelPayment(PAYMENT_ID);
    }

    @Test
    @DisplayName("존재하지 않는 매점 상품을 주문하려고 하면 PRODUCT_NOT_FOUND 예외가 발생한다")
    void createOrder_Fail_ProductNotFound() {
        // Given (준비)
        User user = User.builder().id(1L).nickname("우혁").build();
        Cinema cinema = Cinema.builder().id(1L).name("CGV 신촌").build();

        // 없는 상품(999번) 주문 요청
        FoodOrderRequest request = new FoodOrderRequest(
                1L, 1L,
                List.of(new FoodOrderRequest.OrderItemRequest(999L, 1))
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(cinemaRepository.findById(1L)).willReturn(Optional.of(cinema));
        given(productRepository.findAllById(List.of(999L))).willReturn(List.of());
        given(paymentService.createFoodOrderPaymentId()).willReturn(PAYMENT_ID);
        given(foodOrderRepository.save(any(FoodOrder.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When (실행) & Then (검증)
        CustomException exception = assertThrows(CustomException.class, () -> {
            concessionService.createOrder(request);
        });

        // 예외가 의도한 에러 코드(PRODUCT_NOT_FOUND)인지 확인
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
        verify(paymentService, never()).requestInstantPayment(any(FoodOrder.class));
    }
}
