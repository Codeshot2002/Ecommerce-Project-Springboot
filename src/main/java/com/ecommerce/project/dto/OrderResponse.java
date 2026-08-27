package com.ecommerce.project.dto;

import java.util.List;

public record OrderResponse(Long orderId, List<OrderItemResponse> items) {
}
