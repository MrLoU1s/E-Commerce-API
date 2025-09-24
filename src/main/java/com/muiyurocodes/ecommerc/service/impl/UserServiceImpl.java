package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.dto.LoginResponseDTO;
import com.muiyurocodes.ecommerc.dto.UserLoginDTO;
import com.muiyurocodes.ecommerc.dto.UserRegistrationDTO;
import com.muiyurocodes.ecommerc.dto.UserResponseDTO;
import com.muiyurocodes.ecommerc.exception.EmailAlreadyExistsException;
import com.muiyurocodes.ecommerc.model.Role;
import com.muiyurocodes.ecommerc.model.User;
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

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

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
}
