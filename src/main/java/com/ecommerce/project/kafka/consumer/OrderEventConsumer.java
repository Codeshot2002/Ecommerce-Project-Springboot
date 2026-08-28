package com.ecommerce.project.kafka.consumer;

import com.ecommerce.project.events.OrderCreatedEvent;
import com.ecommerce.project.service.OrderEventConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final OrderEventConsumerService consumerService;

    @KafkaListener(
            topics = "order-events",
            groupId = "order-status-service"
    )
    public void consume(OrderCreatedEvent event) {
        consumerService.process(event);
    }
}
