package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Integer> {
    boolean existsByNickname(String nickname);
    boolean existsByJerseyNumber(int jerseyNumber);

    Optional<PlayerProfile> findByUserDetailsId(Long userDetailsId);
}