package com.surohi.backend.cric_scorer.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordVerifyOtpRequest {
    @NotBlank
    private String loginId;

    @NotBlank
    private String channel; // EMAIL / PHONE

    @NotBlank
    private String otp;
}

