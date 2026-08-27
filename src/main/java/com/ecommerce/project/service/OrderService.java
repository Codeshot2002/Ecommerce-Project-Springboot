package com.ecommerce.project.service;

import com.ecommerce.project.dto.PlaceOrderRequest;
import com.ecommerce.project.dto.PlaceOrderResponse;
import com.ecommerce.project.dto.OrderResponse;
import com.ecommerce.project.models.Order;
import com.ecommerce.project.models.OrderItem;

import java.util.List;

public interface OrderService {
    public List<OrderItem> getAllOrderItems(Long orderId, Long categoryId, Long productId);
    List<OrderResponse> getAllOrders();
    PlaceOrderResponse placeOrder(PlaceOrderRequest request);
}
