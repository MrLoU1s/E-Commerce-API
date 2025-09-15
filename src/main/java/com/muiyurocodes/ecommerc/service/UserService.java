package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.UserDTO;
import com.muiyurocodes.ecommerc.dto.UserRegistrationDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {
    UserDTO registerUser(UserRegistrationDTO registrationDTO);

    Optional<UserDTO> findUserByEmail(String email);
}
