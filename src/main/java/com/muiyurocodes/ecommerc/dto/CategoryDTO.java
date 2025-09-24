package com.muiyurocodes.ecommerc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;
}
