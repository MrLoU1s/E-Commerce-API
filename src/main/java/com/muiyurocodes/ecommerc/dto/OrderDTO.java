package com.muiyurocodes.ecommerc.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Status is required")
    private String status;

    @PastOrPresent(message = "Order date must be in the past or present")
    private LocalDateTime orderDate;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private List<OrderItemDTO> orderItems = new ArrayList<>();

    private BigDecimal totalPrice;
}
