package com.ceos23.cgv.domain.concession.service;

import com.ceos23.cgv.domain.cinema.entity.Cinema;
import com.ceos23.cgv.domain.cinema.repository.CinemaRepository;
import com.ceos23.cgv.domain.concession.dto.FoodOrderRequest;
import com.ceos23.cgv.domain.concession.entity.FoodOrder;
import com.ceos23.cgv.domain.concession.entity.Inventory;
import com.ceos23.cgv.domain.concession.entity.OrderItem;
import com.ceos23.cgv.domain.concession.entity.Product;
import com.ceos23.cgv.domain.concession.enums.FoodOrderStatus;
import com.ceos23.cgv.domain.concession.enums.ProductCategory;
import com.ceos23.cgv.domain.concession.repository.FoodOrderRepository;
import com.ceos23.cgv.domain.concession.repository.InventoryRepository;
import com.ceos23.cgv.domain.concession.repository.OrderItemRepository;
import com.ceos23.cgv.domain.concession.repository.ProductRepository;
import com.ceos23.cgv.domain.payment.service.PaymentService;
import com.ceos23.cgv.domain.user.entity.User;
import com.ceos23.cgv.domain.user.repository.UserRepository;
import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConcessionService {

    private final ProductRepository productRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CinemaRepository cinemaRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentService paymentService;
    private final TransactionTemplate transactionTemplate;

    public ConcessionService(ProductRepository productRepository,
                             FoodOrderRepository foodOrderRepository,
                             OrderItemRepository orderItemRepository,
                             UserRepository userRepository,
                             CinemaRepository cinemaRepository,
                             InventoryRepository inventoryRepository,
                             PaymentService paymentService,
                             PlatformTransactionManager transactionManager) {
        this.productRepository = productRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.cinemaRepository = cinemaRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentService = paymentService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * [GET] 매점의 모든 상품 목록 조회
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * [POST] 매점 상품 주문하기 (복합 로직)
     */
    public FoodOrder createOrder(FoodOrderRequest request) {
        FoodOrder pendingOrder = transactionTemplate.execute(status ->
                createPendingOrder(request)
        );

        try {
            paymentService.requestInstantPayment(pendingOrder);
        } catch (CustomException e) {
            cancelPendingOrder(pendingOrder.getPaymentId());
            throw e;
        } catch (RuntimeException e) {
            cancelPendingOrder(pendingOrder.getPaymentId());
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }

        try {
            return transactionTemplate.execute(status -> completePaidOrder(pendingOrder.getPaymentId()));
        } catch (RuntimeException e) {
            compensatePaidOrder(pendingOrder.getPaymentId());
            throw e;
        }
    }

    private FoodOrder createPendingOrder(FoodOrderRequest request) {
        User user = findUser(request.userId());
        Cinema cinema = findCinema(request.cinemaId());
        FoodOrder foodOrder = FoodOrder.create(user, cinema, paymentService.createFoodOrderPaymentId());
        foodOrderRepository.save(foodOrder);

        Map<Long, Product> productMap = loadProductMap(request);
        List<OrderItem> orderItems = createOrderItems(request, foodOrder, productMap);
        orderItemRepository.saveAll(orderItems);

        foodOrder.calculateTotalPrice(orderItems);

        return foodOrder;
    }

    private FoodOrder completePaidOrder(String paymentId) {
        FoodOrder foodOrder = findOrderByPaymentId(paymentId);
        foodOrder.validatePending();

        List<OrderItem> orderItems = orderItemRepository.findByFoodOrderId(foodOrder.getId());
        decreaseInventoryStocks(foodOrder.getCinema().getId(), orderItems);
        foodOrder.completePayment();

        return foodOrder;
    }

    private void cancelPendingOrder(String paymentId) {
        transactionTemplate.executeWithoutResult(status -> {
            FoodOrder foodOrder = findOrderByPaymentId(paymentId);
            foodOrder.cancel();
        });
    }

    private void compensatePaidOrder(String paymentId) {
        try {
            paymentService.cancelPayment(paymentId);
        } catch (RuntimeException e) {
            log.error("매점 외부 결제 취소 보상 처리에 실패했습니다. paymentId={}", paymentId, e);
        }

        cancelPendingOrder(paymentId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Cinema findCinema(Long cinemaId) {
        return cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new CustomException(ErrorCode.CINEMA_NOT_FOUND));
    }

    private FoodOrder findOrderByPaymentId(String paymentId) {
        return foodOrderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOOD_ORDER_NOT_FOUND));
    }

    private Map<Long, Product> loadProductMap(FoodOrderRequest request) {
        List<Long> productIds = request.orderItems().stream()
                .map(FoodOrderRequest.OrderItemRequest::productId)
                .toList();

        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
    }

    private List<OrderItem> createOrderItems(FoodOrderRequest request, FoodOrder foodOrder,
                                             Map<Long, Product> productMap) {
        return request.orderItems().stream()
                .map(itemReq -> createOrderItem(foodOrder, productMap, itemReq))
                .toList();
    }

    private OrderItem createOrderItem(FoodOrder foodOrder, Map<Long, Product> productMap,
                                      FoodOrderRequest.OrderItemRequest itemReq) {
        Product product = getRequiredProduct(productMap, itemReq.productId());

        return OrderItem.create(foodOrder, product, itemReq.quantity());
    }

    private Product getRequiredProduct(Map<Long, Product> productMap, Long productId) {
        Product product = productMap.get(productId);
        if (product == null) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return product;
    }

    private void decreaseInventoryStocks(Long cinemaId, List<OrderItem> orderItems) {
        orderItems.stream()
                .sorted(Comparator.comparing(orderItem -> orderItem.getProduct().getId()))
                .forEach(orderItem ->
                        decreaseInventoryStock(cinemaId, orderItem.getProduct().getId(), orderItem.getQuantity())
                );
    }

    private void decreaseInventoryStock(Long cinemaId, Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByCinemaIdAndProductIdForUpdate(cinemaId, productId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVENTORY_SHORTAGE));

        inventory.removeStock(quantity);
    }

    /**
     * [POST] 새로운 매점 상품 등록 (관리자용)
     */
    public Product createProduct(String name, int price, String description,
                                 String origin, String ingredient,
                                 Boolean pickupPossible, ProductCategory category) {
        Product product = Product.create(name, price, description, origin, ingredient, pickupPossible, category);
        return transactionTemplate.execute(status -> productRepository.save(product));
    }

    /**
     * [GET] 특정 유저의 매점 주문 내역 조회
     */
    public List<FoodOrder> getOrdersByUserId(Long userId) {
        // N+1 문제를 방지하는 페치 조인 메서드 호출
        return foodOrderRepository.findByUserIdAndStatusWithFetchJoin(userId, FoodOrderStatus.COMPLETED);
    }
}
