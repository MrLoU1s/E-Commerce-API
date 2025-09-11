package com.muiyurocodes.ecommerc.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class CartDTO {

    private Long id;

    @Valid
    private List<CartItemDTO> items;
}
