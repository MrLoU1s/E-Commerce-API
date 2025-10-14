package com.muiyurocodes.ecommerc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for admin dashboard statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    
    // Sales statistics
    private BigDecimal totalSales;
    private long orderCount;
    private BigDecimal averageOrderValue;
    
    // User statistics
    private long userCount;
    private long newUsersToday;
    
    // Product statistics
    private long productCount;
    private List<ProductResponseDTO> lowStockProducts;
    
    // Recent orders
    private List<OrderDTO> recentOrders;
}