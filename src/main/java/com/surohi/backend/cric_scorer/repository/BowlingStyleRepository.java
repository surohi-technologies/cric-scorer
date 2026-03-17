package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.BowlingStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BowlingStyleRepository extends JpaRepository<BowlingStyle, Integer> {
}

