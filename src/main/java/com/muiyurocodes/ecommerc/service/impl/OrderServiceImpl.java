package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.OrderDTO;
import com.muiyurocodes.ecommerc.dto.OrderItemDTO;
import com.muiyurocodes.ecommerc.exception.InsufficientStockException;
import com.muiyurocodes.ecommerc.exception.UserNotFoundException;
import com.muiyurocodes.ecommerc.model.*;
import com.muiyurocodes.ecommerc.repository.OrderRepository;
import com.muiyurocodes.ecommerc.repository.ProductRepository;
import com.muiyurocodes.ecommerc.repository.UserRepository;
import com.muiyurocodes.ecommerc.service.CartService;
import com.muiyurocodes.ecommerc.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;

    @Override
    public OrderDTO placeOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        com.muiyurocodes.ecommerc.dto.CartDTO cartDTO = cartService.getCartForUser(userId);
        if (cartDTO.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart.");
        }

        // 1. Validate stock for all items
        for (com.muiyurocodes.ecommerc.dto.CartItemDTO item : cartDTO.getItems()) {
            productRepository.findByIdAndStockQuantityGreaterThan(item.getProductId(), item.getQuantity() - 1)
                    .orElseThrow(() -> new InsufficientStockException("Not enough stock for product: " + item.getProductName()));
        }

        // 2. Create and save the order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setShippingAddress("Default Shipping Address"); // Placeholder

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 3. Create OrderItems and decrement stock
        for (com.muiyurocodes.ecommerc.dto.CartItemDTO cartItemDTO : cartDTO.getItems()) {
            Product product = productRepository.findById(cartItemDTO.getProductId()).get(); // Already validated stock
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);

            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(cartItemDTO.getQuantity())));

            // Decrement stock
            product.setStockQuantity(product.getStockQuantity() - cartItemDTO.getQuantity());
            productRepository.save(product);
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        // 4. Clear the cart
        cartService.clearCart(userId);

        return convertToOrderDTO(savedOrder);
    }

    @Override
    public Page<OrderDTO> getOrderHistoryForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Page<Order> orderPage = orderRepository.findByUser(user, pageable);
        return orderPage.map(this::convertToOrderDTO);
    }

    @Override
    public Optional<OrderDTO> getOrderDetails(Long userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return orderRepository.findByIdAndUser(orderId, user)
                .map(this::convertToOrderDTO);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderDTO.setUserId(order.getUser().getId());
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList());
        orderDTO.setOrderItems(itemDTOs);
        return orderDTO;
    }
}
