package com.muiyurocodes.ecommerc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderDTO {

    private Long id;

    @Min(value = 0, message = "Total amount must be non-negative")
    private Double totalAmount;

    @NotNull(message = "Status is required")
    private String status;

    @PastOrPresent(message = "Order date must be in the past or present")
    private LocalDateTime orderDate;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
}
