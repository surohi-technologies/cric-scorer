package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    boolean existsByEmailIdIgnoreCase(String emailId);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByUserNameIgnoreCase(String userName);
    Optional<UserDetail> findByEmailIdIgnoreCase(String emailId);
    Optional<UserDetail> findByPhoneNumber(String phoneNumber);
    Optional<UserDetail> findByUserNameIgnoreCase(String userName);
}
