package com.ecommerce.project.service;

import com.ecommerce.project.dto.PlaceOrderItemRequest;
import com.ecommerce.project.dto.PlaceOrderRequest;
import com.ecommerce.project.dto.PlaceOrderResponse;
import com.ecommerce.project.dto.OrderItemResponse;
import com.ecommerce.project.dto.OrderResponse;
import com.ecommerce.project.events.OrderCreatedEvent;
import com.ecommerce.project.kafka.producer.OrderEventProducer;
import com.ecommerce.project.models.Order;
import com.ecommerce.project.models.OrderItem;
import com.ecommerce.project.repositories.OrderItemRepository;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    private final OrderEventProducer orderEventProducer;

    public OrderServiceImpl(OrderItemRepository orderItemRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository, OrderEventProducer orderEventProducer) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Override
    public List<OrderItem> getAllOrderItems(Long orderId, Long categoryId, Long productId) {
        return orderItemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getOrderItems().stream()
                                .map(item -> new OrderItemResponse(
                                        item.getProduct().getId(), item.getQuantity()))
                                .toList(), order.getStatus()))
                .toList();
    }

    @Override
    @Transactional
    public PlaceOrderResponse placeOrder(PlaceOrderRequest request) {
        Order order = new Order();

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
                1L,
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
