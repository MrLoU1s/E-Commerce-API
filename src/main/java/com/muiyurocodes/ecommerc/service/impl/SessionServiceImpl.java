package com.muiyurocodes.ecommerc.service.impl;

import com.muiyurocodes.ecommerc.model.Session;
import com.muiyurocodes.ecommerc.repository.SessionRepository;
import com.muiyurocodes.ecommerc.security.JwtTokenProvider;
import com.muiyurocodes.ecommerc.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Session saveRefreshToken(String email, String refreshToken) {
        // First, clear any old refresh tokens for the user to ensure only one active session.
        sessionRepository.deleteAllByEmail(email);
        // Extract expiration from the token itself
        Instant expiryDate = jwtTokenProvider.extractExpiration(refreshToken).toInstant();
        Session newSession = new Session(email, refreshToken, expiryDate);
        return sessionRepository.save(newSession);
    }

    @Override
    public Optional<Session> findByEmailAndRefreshToken(String email, String refreshToken) {
        return sessionRepository.findByEmailAndRefreshToken(email, refreshToken);
    }

    @Override
    public void deleteByEmailAndRefreshToken(String email, String refreshToken) {
        sessionRepository.findByEmailAndRefreshToken(email, refreshToken)
                .ifPresent(sessionRepository::delete);
    }

    @Override
    public void deleteAllByEmail(String email) {
        sessionRepository.deleteAllByEmail(email);
    }
}
