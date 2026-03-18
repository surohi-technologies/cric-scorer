package com.surohi.backend.cric_scorer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationOtpVerifyRequest {
    @NotNull
    private Long userId;

    @NotBlank
    private String channel; // EMAIL / PHONE

    @NotBlank
    private String otp;
}

