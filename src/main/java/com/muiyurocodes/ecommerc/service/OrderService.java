package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface OrderService {
    OrderDTO placeOrder(Long userId);

    Page<OrderDTO> getOrderHistoryForUser(Long userId, Pageable pageable);

    Optional<OrderDTO> getOrderDetails(Long userId, Long orderId);
}
