package com.surohi.backend.cric_scorer.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String sessionKey;
    private long idleTimeoutSeconds;
    private Long userId;
    private String userName;
    private boolean profileCompleted;
    private boolean firstTimeLogin;
    private String nextAction; // COMPLETE_PROFILE | NONE
}
