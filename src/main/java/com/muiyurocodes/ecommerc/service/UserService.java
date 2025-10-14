package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponseDTO registerUser(UserRegistrationDTO registrationDTO);

    LoginResponseDTO loginUser(UserLoginDTO loginDTO);

    // Profile management
    UserResponseDTO getUserProfile(Long userId);

    UserResponseDTO updateUserProfile(Long userId, ProfileUpdateDTO profileUpdateDTO);

    // Address management
    AddressDTO addAddress(Long userId, AddressDTO addressDTO);

    AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO);

    void deleteAddress(Long userId, Long addressId);

    List<AddressDTO> getUserAddresses(Long userId);

    List<AddressDTO> getUserAddressesByType(Long userId, String addressType);

    AddressDTO getDefaultAddress(Long userId, String addressType);

    AddressDTO setDefaultAddress(Long userId, Long addressId, String addressType);

    // User management (admin)
    Page<UserResponseDTO> getAllUsers(String role, Pageable pageable);

    UserResponseDTO getUserDetails(Long userId);

    UserResponseDTO updateUserRole(Long userId, String role);
}
