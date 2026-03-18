package com.surohi.backend.cric_scorer.repository;

import com.surohi.backend.cric_scorer.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {

    @Query("select o from UserOtp o where o.user.id=:userId and o.purpose=:purpose and o.channel=:channel and o.consumedAt is null order by o.createdAt desc")
    Optional<UserOtp> findLatestActive(@Param("userId") Long userId,
                                       @Param("purpose") String purpose,
                                       @Param("channel") String channel);

    @Modifying
    @Query("update UserOtp o set o.consumedAt=:now where o.user.id=:userId and o.purpose=:purpose and o.channel=:channel and o.consumedAt is null")
    int consumeAllActive(@Param("userId") Long userId,
                         @Param("purpose") String purpose,
                         @Param("channel") String channel,
                         @Param("now") Instant now);
}

