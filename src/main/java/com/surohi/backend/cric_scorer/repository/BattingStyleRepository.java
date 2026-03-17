package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.BattingStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattingStyleRepository extends JpaRepository<BattingStyle, Integer> {
}

