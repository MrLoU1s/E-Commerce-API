package com.muiyurocodes.ecommerc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating product stock quantities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockUpdateDTO {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private Integer stockQuantity;
    
    private String note; // Optional note about the stock update
}