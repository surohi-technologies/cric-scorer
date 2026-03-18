package com.surohi.backend.cric_scorer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 72)
    private String confirmPassword;
}

