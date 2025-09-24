package com.muiyurocodes.ecommerc.controller;

import com.muiyurocodes.ecommerc.dto.LoginResponseDTO;
import com.muiyurocodes.ecommerc.dto.UserLoginDTO;
import com.muiyurocodes.ecommerc.dto.UserRegistrationDTO;
import com.muiyurocodes.ecommerc.dto.UserResponseDTO;
import com.muiyurocodes.ecommerc.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}
