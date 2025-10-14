package com.muiyurocodes.ecommerc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for sales reporting data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO {
    
    // Report metadata
    private LocalDate startDate;
    private LocalDate endDate;
    private String groupBy; // "day", "week", or "month"
    
    // Summary statistics
    private BigDecimal totalSales;
    private long totalOrders;
    private BigDecimal averageOrderValue;
    
    // Time series data for charts
    private Map<String, BigDecimal> salesByPeriod; // Key is date/week/month, value is sales amount
    private Map<String, Long> ordersByPeriod; // Key is date/week/month, value is order count
    
    // Top selling products
    private List<ProductSalesDTO> topSellingProducts;
    
    // Top spending customers
    private List<CustomerSalesDTO> topSpendingCustomers;
    
    /**
     * DTO for product sales data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesDTO {
        private Long productId;
        private String productName;
        private long quantitySold;
        private BigDecimal totalRevenue;
    }
    
    /**
     * DTO for customer sales data.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSalesDTO {
        private Long userId;
        private String userEmail;
        private String userName;
        private long orderCount;
        private BigDecimal totalSpent;
    }
}