package com.muiyurocodes.ecommerc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 1024)
    private String refreshToken;

    @Column(nullable = false)
    private Instant expiryDate;

    public Session(String email, String refreshToken, Instant expiryDate) {
        this.email = email;
        this.refreshToken = refreshToken;
        this.expiryDate = expiryDate;
    }
}
