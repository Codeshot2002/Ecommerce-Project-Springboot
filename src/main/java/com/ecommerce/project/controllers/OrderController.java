package com.ecommerce.project.controllers;

import com.ecommerce.project.dto.PlaceOrderRequest;
import com.ecommerce.project.dto.PlaceOrderResponse;
import com.ecommerce.project.dto.OrderResponse;
import com.ecommerce.project.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/api/orders")
    public List<OrderResponse> getAllOrders(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return orderService.getAllOrders(authentication.getName(), isAdmin);
    }

    @PostMapping("/api/orders")
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request, Authentication authentication) {
        PlaceOrderResponse response = orderService.placeOrder(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
