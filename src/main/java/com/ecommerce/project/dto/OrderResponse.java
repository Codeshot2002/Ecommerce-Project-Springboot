package com.ecommerce.project.dto;

import com.ecommerce.project.Enums.OrderStatus;

import java.util.List;

public record OrderResponse(Long orderId, List<OrderItemResponse> items, OrderStatus status) {
}
