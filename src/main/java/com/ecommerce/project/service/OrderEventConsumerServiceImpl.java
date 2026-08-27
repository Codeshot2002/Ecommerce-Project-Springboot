package com.ecommerce.project.service;

import com.ecommerce.project.events.OrderCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumerServiceImpl implements OrderEventConsumerService {

    @Override
    public void process(OrderCreatedEvent event) {
        System.out.println("Order has been processed");
        System.out.println("Email sending for order Id : " + event.orderId());
        System.out.println("Order placed by user : " + event.userId());
    }
}
