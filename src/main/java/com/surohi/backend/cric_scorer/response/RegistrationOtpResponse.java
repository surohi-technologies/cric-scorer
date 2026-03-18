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
public class RegistrationOtpResponse {
    private String message;
    private boolean verified;
    private boolean active;
    private List<String> requiredChannels;
    private List<String> verifiedChannels;
}

