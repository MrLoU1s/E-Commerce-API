package com.muiyurocodes.ecommerc.repository;

import com.muiyurocodes.ecommerc.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByEmailAndRefreshToken(String email, String refreshToken);

    void deleteAllByEmail(String email);
}
