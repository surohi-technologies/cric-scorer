package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.UserPasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPasswordHistoryRepository extends JpaRepository<UserPasswordHistory, Long> {

    List<UserPasswordHistory> findTop3ByUser_IdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("delete from UserPasswordHistory h where h.user.id=:userId and h.id not in :keepIds")
    int deleteAllExcept(@Param("userId") Long userId, @Param("keepIds") List<Long> keepIds);
}
