package com.ecommerce.project.service;

import com.ecommerce.project.dto.*;
import com.ecommerce.project.events.OrderCreatedEvent;
import com.ecommerce.project.kafka.producer.OrderEventProducer;
import com.ecommerce.project.models.Order;
import com.ecommerce.project.models.OrderItem;
import com.ecommerce.project.models.Product;
import com.ecommerce.project.models.User;
import com.ecommerce.project.repositories.OrderItemRepository;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(OrderItemRepository orderItemRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository, UserRepository userRepository, OrderEventProducer orderEventProducer) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    public List<OrderItem> getAllOrderItems(Long orderId, Long categoryId, Long productId) {
        return orderItemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(String email, boolean isAdmin) {
        User customer = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        List<Order> orders = isAdmin ? orderRepository.findAll() : orderRepository.findByCustomerOrderByIdDesc(customer);
        List<Long> productIds = orders.stream().flatMap(x -> x.getOrderItems().stream()).map(orderItem -> orderItem.getProduct().getId()).toList();
        Map<Long, Product> productById = productRepository.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        return orders.stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getOrderItems().stream()
                                .map(orderItem -> {
                                    Product product = productById.get(orderItem.getProduct().getId());
                                    if (product == null) {
                                        return null;
                                    } else {
                                        return new OrderItemResponse(product.getId(), product.getName(), orderItem.getQuantity());
                                    }
                                }).filter(Objects::nonNull)
                                .toList(), order.getStatus()))
                .toList();
    }

    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request, String email) {
        Order order = new Order();
        order.setCustomer(userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")));

        for (PlaceOrderItemRequest requestedItem : request.items()) {
            int changedRows = productRepository.decreaseQuantityIfAvailable(
                    requestedItem.productId(), requestedItem.quantity());

            if (changedRows == 0) {
                // The transaction rolls back stock decreased for earlier items.
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Product " + requestedItem.productId()
                                + " does not exist or has insufficient stock");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(productRepository.getReferenceById(requestedItem.productId()));
            orderItem.setQuantity(requestedItem.quantity());
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        // Kafka event creation for order placed
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getCustomer().getId(),
                Instant.now()
        );
        try {
            orderEventProducer.publishOrderCreated(event);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return new PlaceOrderResponse(savedOrder.getId(), "Order placed successfully");
    }
}
