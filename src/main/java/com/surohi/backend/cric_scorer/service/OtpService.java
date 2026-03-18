package com.surohi.backend.cric_scorer.service;

import com.surohi.backend.cric_scorer.constants.OtpChannel;
import com.surohi.backend.cric_scorer.constants.OtpPurpose;
import com.surohi.backend.cric_scorer.entity.UserDetail;

import java.util.List;

public interface OtpService {
    List<OtpChannel> sendRegistrationOtps(UserDetail user);

    /**
     * Sends a PASSWORD_RESET OTP to the requested channel (if user has destination).
     * Returns true if an OTP was actually sent.
     */
    boolean sendPasswordResetOtp(UserDetail user, OtpChannel channel);

    boolean verifyOtp(Long userId, OtpPurpose purpose, OtpChannel channel, String otp);
}

