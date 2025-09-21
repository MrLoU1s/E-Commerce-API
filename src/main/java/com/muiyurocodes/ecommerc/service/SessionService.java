package com.muiyurocodes.ecommerc.service;

import com.muiyurocodes.ecommerc.model.Session;
import org.springframework.stereotype.Service;

import java.util.Optional;


public interface SessionService {

    Session saveRefreshToken(String email, String refreshToken);

    Optional<Session> findByEmailAndRefreshToken(String email, String refreshToken);

    void deleteByEmailAndRefreshToken(String email, String refreshToken);

    void deleteAllByEmail(String email);
}
