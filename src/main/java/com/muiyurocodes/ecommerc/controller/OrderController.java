package com.muiyurocodes.ecommerc.controller;

import com.muiyurocodes.ecommerc.dto.OrderDTO;
import com.muiyurocodes.ecommerc.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Place a new order using the items in the user's cart
     * @param userId The ID of the user placing the order
     * @return The created order
     */
    @PostMapping("/users/{userId}")
    public ResponseEntity<OrderDTO> placeOrder(@PathVariable Long userId) {
        OrderDTO order = orderService.placeOrder(userId);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    /**
     * Get the order history for a user
     * @param userId The ID of the user
     * @param pageable Pagination parameters
     * @return A page of orders
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<OrderDTO>> getOrderHistory(
            @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<OrderDTO> orders = orderService.getOrderHistoryForUser(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get details for a specific order
     * @param userId The ID of the user
     * @param orderId The ID of the order
     * @return The order details if found
     */
    @GetMapping("/users/{userId}/{orderId}")
    public ResponseEntity<OrderDTO> getOrderDetails(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        Optional<OrderDTO> order = orderService.getOrderDetails(userId, orderId);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}