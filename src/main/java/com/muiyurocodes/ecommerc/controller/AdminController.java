package com.muiyurocodes.ecommerc.controller;

import com.muiyurocodes.ecommerc.dto.*;
import com.muiyurocodes.ecommerc.service.OrderService;
import com.muiyurocodes.ecommerc.service.ProductService;
import com.muiyurocodes.ecommerc.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for admin-specific operations including dashboard, sales reporting,
 * user management, and inventory management.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;

    public AdminController(OrderService orderService, UserService userService, ProductService productService) {
        this.orderService = orderService;
        this.userService = userService;
        this.productService = productService;
    }

    /**
     * Dashboard endpoint that provides summary statistics for the admin dashboard.
     * Includes total sales, order count, user count, and low stock products.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboardStats() {
        DashboardDTO dashboardStats = orderService.getDashboardStats();
        return ResponseEntity.ok(dashboardStats);
    }

    /**
     * Sales reporting endpoint that provides sales data for a specified date range.
     * Can be filtered by date range and grouped by day, week, or month.
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesReportDTO> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String groupBy) {
        
        SalesReportDTO salesReport = orderService.getSalesReport(startDate, endDate, groupBy);
        return ResponseEntity.ok(salesReport);
    }

    /**
     * User management endpoint that provides a paginated list of all users.
     * Can be sorted by various fields and filtered by role.
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponseDTO> users = userService.getAllUsers(role, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * User management endpoint that provides details for a specific user.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserDetails(@PathVariable Long userId) {
        UserResponseDTO user = userService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * User management endpoint that updates a user's role.
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> roleUpdate) {
        
        String newRole = roleUpdate.get("role");
        UserResponseDTO updatedUser = userService.updateUserRole(userId, newRole);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Inventory management endpoint that provides a list of products with low stock.
     */
    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<ProductResponseDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "5") int threshold) {
        
        List<ProductResponseDTO> lowStockProducts = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(lowStockProducts);
    }

    /**
     * Inventory management endpoint that updates stock quantities for multiple products.
     */
    @PutMapping("/inventory/stock")
    public ResponseEntity<List<ProductResponseDTO>> updateProductStock(
            @RequestBody List<ProductStockUpdateDTO> stockUpdates) {
        
        List<ProductResponseDTO> updatedProducts = productService.updateProductStock(stockUpdates);
        return ResponseEntity.ok(updatedProducts);
    }

    /**
     * Order management endpoint that provides a paginated list of all orders.
     * Can be sorted by various fields and filtered by status.
     */
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDTO> orders = orderService.getAllOrders(status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Order management endpoint that updates the status of an order.
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusUpdate) {
        
        String newStatus = statusUpdate.get("status");
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}