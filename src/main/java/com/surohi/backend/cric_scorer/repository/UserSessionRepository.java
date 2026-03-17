package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findBySessionKeyHashAndIsActiveTrue(String sessionKeyHash);
}

