package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_otp", schema = "public",
        indexes = {
                @Index(name = "ix_user_otp_user_purpose_active", columnList = "user_id,purpose,channel,consumed_at"),
                @Index(name = "ix_user_otp_expires", columnList = "expires_at")
        })
@Getter
@Setter
public class UserOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserDetail user;

    @Column(name = "channel", nullable = false, length = 10)
    private String channel; // EMAIL | PHONE

    @Column(name = "destination", nullable = false, length = 255)
    private String destination;

    @Column(name = "otp_hash", nullable = false, length = 64)
    private String otpHash;

    @Column(name = "purpose", nullable = false, length = 30)
    private String purpose; // REGISTRATION_VERIFY | PASSWORD_RESET

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 5;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;
}

