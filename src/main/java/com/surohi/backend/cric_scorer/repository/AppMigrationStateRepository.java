package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.AppMigrationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppMigrationStateRepository extends JpaRepository<AppMigrationState, Integer> {
}
