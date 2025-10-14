package com.muiyurocodes.ecommerc.controller;

import com.muiyurocodes.ecommerc.dto.*;
import com.muiyurocodes.ecommerc.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Authentication Endpoints

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        UserResponseDTO registeredUser = userService.registerUser(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody UserLoginDTO loginDTO) {
        LoginResponseDTO response = userService.loginUser(loginDTO);
        return ResponseEntity.ok(response);
    }
    
    // Profile Management Endpoints
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable Long userId) {
        UserResponseDTO userProfile = userService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody ProfileUpdateDTO profileUpdateDTO) {
        UserResponseDTO updatedProfile = userService.updateUserProfile(userId, profileUpdateDTO);
        return ResponseEntity.ok(updatedProfile);
    }
    
    // Address Management Endpoints
    
    @GetMapping("/{userId}/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable Long userId) {
        List<AddressDTO> addresses = userService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }
    
    @GetMapping("/{userId}/addresses/type/{addressType}")
    public ResponseEntity<List<AddressDTO>> getUserAddressesByType(
            @PathVariable Long userId,
            @PathVariable String addressType) {
        List<AddressDTO> addresses = userService.getUserAddressesByType(userId, addressType);
        return ResponseEntity.ok(addresses);
    }
    
    @GetMapping("/{userId}/addresses/default/{addressType}")
    public ResponseEntity<AddressDTO> getDefaultAddress(
            @PathVariable Long userId,
            @PathVariable String addressType) {
        AddressDTO address = userService.getDefaultAddress(userId, addressType);
        if (address == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(address);
    }
    
    @PostMapping("/{userId}/addresses")
    public ResponseEntity<AddressDTO> addAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO savedAddress = userService.addAddress(userId, addressDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
    }
    
    @PutMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO updatedAddress = userService.updateAddress(userId, addressId, addressDTO);
        return ResponseEntity.ok(updatedAddress);
    }
    
    @DeleteMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {
        userService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{userId}/addresses/{addressId}/default/{addressType}")
    public ResponseEntity<AddressDTO> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @PathVariable String addressType) {
        AddressDTO defaultAddress = userService.setDefaultAddress(userId, addressId, addressType);
        return ResponseEntity.ok(defaultAddress);
    }
}