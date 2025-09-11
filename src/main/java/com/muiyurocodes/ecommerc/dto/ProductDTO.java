package com.muiyurocodes.ecommerc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @Min(value = 0, message = "Price must be non-negative")
    private Double price;

    @Min(value = 0, message = "Stock quantity must be non-negative")
    private Integer stockQuantity;

    private String category;
    private String imageUrl;
}
