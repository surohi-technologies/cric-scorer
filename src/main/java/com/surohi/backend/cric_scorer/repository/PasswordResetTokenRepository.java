package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("select t from PasswordResetToken t where t.tokenHash=:hash and t.consumedAt is null")
    Optional<PasswordResetToken> findActiveByHash(@Param("hash") String hash);
}

