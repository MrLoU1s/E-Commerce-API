package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.muiyurocodes.ecommerc.exception.EmailAlreadyExistsException;
import com.muiyurocodes.ecommerc.exception.UserNotFoundException;
import com.muiyurocodes.ecommerc.model.Address;
import com.muiyurocodes.ecommerc.model.Role;
import com.muiyurocodes.ecommerc.model.User;
import com.muiyurocodes.ecommerc.repository.AddressRepository;
import com.muiyurocodes.ecommerc.repository.UserRepository;
import com.muiyurocodes.ecommerc.security.JwtTokenProvider;
import com.muiyurocodes.ecommerc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    // Profile management methods

    @Override
    public UserResponseDTO getUserProfile(Long userId) {
        User user = findUserById(userId);
        return modelMapper.map(user, UserResponseDTO.class);
    }

    @Override
    public UserResponseDTO updateUserProfile(Long userId, ProfileUpdateDTO profileUpdateDTO) {
        User user = findUserById(userId);

        // Update basic profile information
        user.setFirstName(profileUpdateDTO.getFirstName());
        user.setLastName(profileUpdateDTO.getLastName());

        // Update password if provided
        if (profileUpdateDTO.getPassword() != null && !profileUpdateDTO.getPassword().isEmpty()) {
            // In a real application, you would validate that confirmPassword matches
            // password
            user.setPassword(passwordEncoder.encode(profileUpdateDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserResponseDTO.class);
    }

    // Address management methods

    @Override
    public AddressDTO addAddress(Long userId, AddressDTO addressDTO) {
        User user = findUserById(userId);

        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);

        // If this is set as default, unset any existing default of the same type
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            unsetDefaultAddress(user, address.getAddressType());
        }

        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO) {
        User user = findUserById(userId);
        Address address = findAddressByIdAndUser(addressId, user);

        // Update address fields
        address.setStreetAddress(addressDTO.getStreetAddress());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setCountry(addressDTO.getCountry());
        address.setPhoneNumber(addressDTO.getPhoneNumber());
        address.setAddressType(addressDTO.getAddressType());

        // If this is set as default, unset any existing default of the same type
        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            unsetDefaultAddress(user, addressDTO.getAddressType());
            address.setIsDefault(true);
        }

        Address updatedAddress = addressRepository.save(address);
        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        User user = findUserById(userId);
        Address address = findAddressByIdAndUser(addressId, user);

        addressRepository.delete(address);
    }

    @Override
    public List<AddressDTO> getUserAddresses(Long userId) {
        User user = findUserById(userId);
        List<Address> addresses = addressRepository.findByUser(user);

        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDTO> getUserAddressesByType(Long userId, String addressType) {
        User user = findUserById(userId);
        List<Address> addresses = addressRepository.findByUserAndAddressType(user, addressType);

        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getDefaultAddress(Long userId, String addressType) {
        User user = findUserById(userId);
        return addressRepository.findByUserAndIsDefaultAndAddressType(user, true, addressType)
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .orElse(null);
    }

    @Override
    public AddressDTO setDefaultAddress(Long userId, Long addressId, String addressType) {
        User user = findUserById(userId);
        Address address = findAddressByIdAndUser(addressId, user);

        // Ensure the address type matches
        if (!address.getAddressType().equals(addressType)) {
            address.setAddressType(addressType);
        }

        // Unset any existing default address of this type
        unsetDefaultAddress(user, addressType);

        // Set this address as default
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    // Helper methods

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private Address findAddressByIdAndUser(Long addressId, User user) {
        return addressRepository.findById(addressId)
                .filter(address -> address.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Address not found or does not belong to user"));
    }

    private void unsetDefaultAddress(User user, String addressType) {
        addressRepository.findByUserAndIsDefaultAndAddressType(user, true, addressType)
                .ifPresent(defaultAddress -> {
                    defaultAddress.setIsDefault(false);
                    addressRepository.save(defaultAddress);
                });
    }

    @Override
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("User with email " + registrationDTO.getEmail() + " already exists.");
        }

        User newUser = modelMapper.map(registrationDTO, User.class);
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(newUser);

        return modelMapper.map(savedUser, UserResponseDTO.class);
    }

    @Override
    public LoginResponseDTO loginUser(UserLoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        UserResponseDTO userResponseDTO = modelMapper.map(user, UserResponseDTO.class);

        return new LoginResponseDTO(accessToken, userResponseDTO);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // User management methods (admin)

    @Override
    public Page<UserResponseDTO> getAllUsers(String role, Pageable pageable) {
        Page<User> users;

        if (role != null && !role.isEmpty()) {
            // If role is provided, filter by role
            Role userRole = Role.valueOf(role);
            users = userRepository.findByRole(userRole, pageable);
        } else {
            // Otherwise, get all users
            users = userRepository.findAll(pageable);
        }

        return users.map(user -> modelMapper.map(user, UserResponseDTO.class));
    }

    @Override
    public UserResponseDTO getUserDetails(Long userId) {
        // We can reuse the getUserProfile method
        return getUserProfile(userId);
    }

    @Override
    public UserResponseDTO updateUserRole(Long userId, String role) {
        User user = findUserById(userId);

        try {
            Role newRole = Role.valueOf(role);
            user.setRole(newRole);
            User updatedUser = userRepository.save(user);
            return modelMapper.map(updatedUser, UserResponseDTO.class);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
