package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.dto.LoginResponseDTO;
import com.muiyurocodes.ecommerc.dto.UserLoginDTO;
import com.muiyurocodes.ecommerc.dto.UserRegistrationDTO;
import com.muiyurocodes.ecommerc.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerUser(UserRegistrationDTO registrationDTO);

    LoginResponseDTO loginUser(UserLoginDTO loginDTO);
}
