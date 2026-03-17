package com.surohi.backend.cric_scorer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "user_session",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_session_key_hash", columnNames = {"session_key_hash"})
        }
)
@Getter
@Setter
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserDetail user;

    @Column(name = "session_key_hash", nullable = false, length = 64)
    private String sessionKeyHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}

