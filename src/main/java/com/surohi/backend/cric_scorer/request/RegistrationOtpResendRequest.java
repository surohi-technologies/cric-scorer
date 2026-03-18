package com.surohi.backend.cric_scorer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationOtpResendRequest {
    @NotNull
    private Long userId;

    // Optional: if null, resend to all required channels
    private String channel; // EMAIL / PHONE
}

