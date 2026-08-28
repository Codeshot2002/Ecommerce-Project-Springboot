package com.ecommerce.project.service;

import com.ecommerce.project.Enums.OrderStatus;
import com.ecommerce.project.events.OrderCreatedEvent;
import com.ecommerce.project.models.Order;
import com.ecommerce.project.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderEventConsumerServiceImpl implements OrderEventConsumerService {
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public OrderEventConsumerServiceImpl(OrderRepository orderRepository, @Qualifier("email") NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void process(OrderCreatedEvent event) {
        if (event == null || event.orderId() == null) {
            throw new IllegalArgumentException("Order-created event must contain an order ID");
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Received an order-created event for unknown order " + event.orderId()));

        if (order.getStatus() == OrderStatus.CREATED) {
            log.info("Ignoring duplicate order-created event {} for order {}", event.eventId(), event.orderId());
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Ignoring order-created event {} for order {} because its status is already {}",
                    event.eventId(), event.orderId(), order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.CREATED);
        log.info("Order {} marked as CREATED from event {}", event.orderId(), event.eventId());
        notificationService.send();
    }
}
