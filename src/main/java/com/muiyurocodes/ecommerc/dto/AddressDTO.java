package com.muiyurocodes.ecommerc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    
    private Long id;
    
    @NotBlank(message = "Street address is required")
    private String streetAddress;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "Postal code is required")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    private String phoneNumber;
    
    @NotNull(message = "Default status is required")
    private Boolean isDefault = false;
    
    @NotBlank(message = "Address type is required")
    private String addressType; // "SHIPPING", "BILLING", etc.
}