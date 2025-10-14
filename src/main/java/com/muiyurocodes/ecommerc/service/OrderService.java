package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.DashboardDTO;
import com.muiyurocodes.ecommerc.dto.OrderDTO;
import com.muiyurocodes.ecommerc.dto.SalesReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public interface OrderService {
    // Customer order operations
    OrderDTO placeOrder(Long userId);

    Page<OrderDTO> getOrderHistoryForUser(Long userId, Pageable pageable);

    Optional<OrderDTO> getOrderDetails(Long userId, Long orderId);

    // Admin dashboard and reporting
    DashboardDTO getDashboardStats();

    SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate, String groupBy);

    // Admin order management
    Page<OrderDTO> getAllOrders(String status, Pageable pageable);

    OrderDTO updateOrderStatus(Long orderId, String status);

    // Webhook handling (for payment status updates), skips validation for
    // simplicity
    void updateOrderStatusFromWebhook(Long orderId, String status);
}
