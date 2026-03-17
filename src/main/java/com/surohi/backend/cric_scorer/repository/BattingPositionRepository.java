package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.BattingPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattingPositionRepository extends JpaRepository<BattingPosition, Integer> {
}

