package com.ecommerce.project.service;

import com.ecommerce.project.events.OrderCreatedEvent;

public interface OrderEventConsumerService {
    public void process(OrderCreatedEvent event);
}
