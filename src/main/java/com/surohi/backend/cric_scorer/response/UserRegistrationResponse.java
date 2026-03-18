package com.surohi.backend.cric_scorer.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {
    private String message;
    private Long userId;
    private String userName;
    private String uniqueIdentifier;

    private boolean verificationRequired;
    private List<String> requiredChannels; // EMAIL / PHONE
}
