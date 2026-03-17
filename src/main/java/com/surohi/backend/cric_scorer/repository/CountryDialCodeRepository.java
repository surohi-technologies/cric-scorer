package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.CountryDialCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryDialCodeRepository extends JpaRepository<CountryDialCode, Long> {
    boolean existsByDialCode(String dialCode);
}

