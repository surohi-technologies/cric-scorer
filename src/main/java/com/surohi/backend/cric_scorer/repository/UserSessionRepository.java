package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findBySessionKeyHashAndIsActiveTrue(String sessionKeyHash);

    @Modifying
    @Query("update UserSession s set s.isActive=false where s.isActive=true and s.lastActivityAt < :cutoff")
    int deactivateExpiredSessions(@Param("cutoff") Instant cutoff);
}
