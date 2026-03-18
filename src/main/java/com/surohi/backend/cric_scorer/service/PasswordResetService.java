package com.surohi.backend.cric_scorer.service;

import com.surohi.backend.cric_scorer.constants.OtpChannel;
import com.surohi.backend.cric_scorer.response.ForgotPasswordStartResponse;
import com.surohi.backend.cric_scorer.response.ForgotPasswordVerifyResponse;

public interface PasswordResetService {
    ForgotPasswordStartResponse start(String loginId, OtpChannel preferredChannel);

    ForgotPasswordVerifyResponse verifyOtp(String loginId, OtpChannel channel, String otp);

    void resetPassword(String resetToken, String newPassword, String confirmPassword);
}

