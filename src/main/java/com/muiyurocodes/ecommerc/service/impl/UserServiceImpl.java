package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.UserRegistrationDTO;
import com.muiyurocodes.ecommerc.dto.UserResponseDTO;
import com.muiyurocodes.ecommerc.exception.EmailAlreadyExistsException;
import com.muiyurocodes.ecommerc.model.Role;
import com.muiyurocodes.ecommerc.model.User;
import com.muiyurocodes.ecommerc.repository.UserRepository;
import com.muiyurocodes.ecommerc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        // 1. Check if user already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("User with email " + registrationDTO.getEmail() + " already exists.");
        }

        // 2. Map DTO to entity
        User newUser = modelMapper.map(registrationDTO, User.class);

        // 3. Encode password
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));

        // 4. Set default role
        newUser.setRole(Role.ROLE_USER);

        // 5. Save user
        User savedUser = userRepository.save(newUser);

        // 6. Map entity to response DTO
        return modelMapper.map(savedUser, UserResponseDTO.class);
    }
}
