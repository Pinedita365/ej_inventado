package com.example.ej_inventado.clases;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiry;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email, LocalDateTime expiry) {
        this.token  = token;
        this.email  = email;
        this.expiry = expiry;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiry);
    }

    public Long getId()              { return id; }
    public String getToken()         { return token; }
    public String getEmail()         { return email; }
    public LocalDateTime getExpiry() { return expiry; }
}
