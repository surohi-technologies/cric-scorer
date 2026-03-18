package com.surohi.backend.cric_scorer.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    /**
     * Username OR phone OR email.
     * We keep it simple for users.
     */
    @NotBlank
    private String loginId;

    // Optional: EMAIL / PHONE (if user has both)
    private String channel;
}
